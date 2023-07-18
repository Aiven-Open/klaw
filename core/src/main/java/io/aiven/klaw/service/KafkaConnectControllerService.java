package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.*;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_KAFKA_CONNECT_ENVS;
import static io.aiven.klaw.model.enums.MailType.CONNECTOR_CLAIM_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.CONNECTOR_CREATE_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.CONNECTOR_DELETE_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.CONNECTOR_REQUEST_APPROVED;
import static io.aiven.klaw.model.enums.MailType.CONNECTOR_REQUEST_DENIED;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawRestException;
import io.aiven.klaw.error.RestErrorResponse;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KlawResourceUtils;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ConnectorConfig;
import io.aiven.klaw.model.ResourceHistory;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.PromotionStatusType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.KafkaConnectorModel;
import io.aiven.klaw.model.requests.KafkaConnectorRequestModel;
import io.aiven.klaw.model.requests.KafkaConnectorRestartModel;
import io.aiven.klaw.model.response.ConnectorOverview;
import io.aiven.klaw.model.response.ConnectorOverviewPerEnv;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.KafkaConnectorModelResponse;
import io.aiven.klaw.model.response.KafkaConnectorRequestsResponseModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

@Service
@Slf4j
public class KafkaConnectControllerService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
  public static final TypeReference<ArrayList<ResourceHistory>> VALUE_TYPE_REF =
      new TypeReference<>() {};
  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  public ApiResponse createConnectorRequest(KafkaConnectorRequestModel connectorRequestModel)
      throws KlawException {
    log.info("createConnectorRequest {}", connectorRequestModel);
    String userName = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_CONNECTORS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    try {
      if (connectorRequestModel.getConnectorConfig() != null
          && connectorRequestModel.getConnectorConfig().trim().length() > 0) {
        JsonNode jsonNode =
            OBJECT_MAPPER.readTree(connectorRequestModel.getConnectorConfig().trim());
        if (!jsonNode.has("tasks.max")) {
          return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_101).build();
        } else if (!jsonNode.has("connector.class")) {
          return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_102).build();
        } else if (!jsonNode.has("topics") && !jsonNode.has("topics.regex")) {
          return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_103).build();
        }

        if (jsonNode.has("topics") && jsonNode.has("topics.regex")) {
          return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_104).build();
        }

        Map<String, Object> resultMap =
            OBJECT_MAPPER.convertValue(jsonNode, new TypeReference<>() {});
        connectorRequestModel.setConnectorConfig(
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap));
      }
    } catch (JsonProcessingException e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }

    connectorRequestModel.setRequestor(userName);
    Integer userTeamId = commonUtilsService.getTeamId(userName);
    connectorRequestModel.setTeamId(userTeamId);
    String envSelected = connectorRequestModel.getEnvironment();

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userName).contains(envSelected)) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_105).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    // tenant filtering
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String syncCluster;
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncKafkaConnectCluster();
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      throw new KlawException(e.getMessage());
    }
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");

    List<KwKafkaConnector> kafkaConnectorList =
        getConnectorsFromName(connectorRequestModel.getConnectorName(), tenantId);

    if (!kafkaConnectorList.isEmpty()
        && !Objects.equals(
            kafkaConnectorList.get(0).getTeamId(), connectorRequestModel.getTeamId())) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_106).build();
    }

    boolean promotionOrderCheck =
        checkInPromotionOrder(connectorRequestModel.getEnvironment(), orderOfEnvs);

    if (!kafkaConnectorList.isEmpty()) {
      if (promotionOrderCheck) {
        int devTopicFound =
            (int)
                kafkaConnectorList.stream()
                    .filter(topic -> Objects.equals(topic.getEnvironment(), syncCluster))
                    .count();
        if (devTopicFound != 1) {
          if (getKafkaConnectEnvDetails(syncCluster) == null) {
            return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_107).build();
          } else {
            return ApiResponse.builder()
                .message(
                    String.format(
                        KAFKA_CONNECT_ERR_108, getKafkaConnectEnvDetails(syncCluster).getName()))
                .build();
          }
        }
      }
    } else if (!Objects.equals(connectorRequestModel.getEnvironment(), syncCluster)) {
      if (promotionOrderCheck) {
        return ApiResponse.builder()
            .success(false)
            .message(
                String.format(
                    KAFKA_CONNECT_ERR_109, getKafkaConnectEnvDetails(syncCluster).getName()))
            .build();
      }
    }

    if (manageDatabase
            .getHandleDbRequests()
            .getConnectorRequests(
                connectorRequestModel.getConnectorName(),
                connectorRequestModel.getEnvironment(),
                RequestStatus.CREATED.value,
                tenantId)
            .size()
        > 0) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_110).build();
    }

    // Ignore connector exists check if Update request
    if (RequestOperationType.UPDATE != connectorRequestModel.getRequestOperationType()) {
      boolean topicExists = false;
      if (!kafkaConnectorList.isEmpty()) {
        topicExists =
            kafkaConnectorList.stream()
                .anyMatch(
                    topicEx ->
                        Objects.equals(
                            topicEx.getEnvironment(), connectorRequestModel.getEnvironment()));
      }
      if (topicExists) {
        return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_111).build();
      }
    }

    KafkaConnectorRequest topicRequestDao = new KafkaConnectorRequest();
    copyProperties(connectorRequestModel, topicRequestDao);
    topicRequestDao.setRequestOperationType(connectorRequestModel.getRequestOperationType().value);
    topicRequestDao.setTenantId(tenantId);

    String result = dbHandle.requestForConnector(topicRequestDao).get("result");
    mailService.sendMail(
        connectorRequestModel.getConnectorName(),
        null,
        "",
        userName,
        null,
        NumberUtils.toInt(topicRequestDao.getApprovingTeamId(), -1),
        dbHandle,
        CONNECTOR_CREATE_REQUESTED,
        commonUtilsService.getLoginUrl());
    if (result.equals(ApiResultStatus.SUCCESS.value)) {
      return ApiResponse.builder().success(true).message(result).build();
    } else {
      return ApiResponse.builder().success(false).message(result).build();
    }
  }

  public List<List<KafkaConnectorModelResponse>> getConnectors(
      String env, String pageNo, String currentPage, String connectorNameSearch, Integer teamId)
      throws Exception {
    log.debug("getConnectors {}", connectorNameSearch);
    List<KafkaConnectorModelResponse> topicListUpdated =
        getConnectorsPaginated(env, pageNo, currentPage, connectorNameSearch, teamId);

    if (topicListUpdated != null && topicListUpdated.size() > 0) {
      updateTeamNamesForDisplay(topicListUpdated);
      return getPagedList(topicListUpdated);
    }

    return null;
  }

  private List<List<KafkaConnectorModelResponse>> getPagedList(
      List<KafkaConnectorModelResponse> topicsList) {

    List<List<KafkaConnectorModelResponse>> newList = new ArrayList<>();
    List<KafkaConnectorModelResponse> innerList = new ArrayList<>();
    int modulusFactor = 3;
    int i = 0;
    for (KafkaConnectorModelResponse topicInfo : topicsList) {

      innerList.add(topicInfo);

      if (i % modulusFactor == (modulusFactor - 1)) {
        newList.add(innerList);
        innerList = new ArrayList<>();
      }
      i++;
    }

    if (innerList.size() > 0) {
      newList.add(innerList);
    }

    return newList;
  }

  private void updateTeamNamesForDisplay(List<KafkaConnectorModelResponse> topicListUpdated) {
    topicListUpdated.forEach(
        topicInfo -> {
          if (topicInfo.getTeamName().length() > 9) {
            topicInfo.setTeamName(topicInfo.getTeamName().substring(0, 8) + "...");
          }
        });
  }

  private List<KafkaConnectorModelResponse> getConnectorsPaginated(
      String env, String pageNo, String currentPage, String connectorNameSearch, Integer teamId) {
    if (connectorNameSearch != null) {
      connectorNameSearch = connectorNameSearch.trim();
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();

    // Get Sync topics

    List<KwKafkaConnector> connectorsFromSOT =
        handleDbRequests.getSyncConnectors(env, teamId, tenantId);
    connectorsFromSOT = getFilteredConnectorsForTenant(connectorsFromSOT);
    List<Env> listAllEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);
    // tenant filtering

    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");
    connectorsFromSOT = groupConnectorsByEnv(connectorsFromSOT);

    List<KwKafkaConnector> connectorsFilteredList = connectorsFromSOT;
    // Filter topics on topic name for search
    if (connectorNameSearch != null && connectorNameSearch.length() > 0) {
      final String topicSearchFilter = connectorNameSearch;
      connectorsFilteredList =
          connectorsFromSOT.stream()
              .filter(
                  topic ->
                      topic
                          .getConnectorName()
                          .toLowerCase()
                          .contains(topicSearchFilter.toLowerCase()))
              .collect(Collectors.toList());

      // searching documentation
      List<KwKafkaConnector> searchDocList =
          connectorsFromSOT.stream()
              .filter(
                  topic ->
                      (topic.getDocumentation() != null
                          && topic
                              .getDocumentation()
                              .toLowerCase()
                              .contains(topicSearchFilter.toLowerCase())))
              .toList();

      connectorsFilteredList.addAll(searchDocList);
      connectorsFilteredList =
          new ArrayList<>(
              connectorsFilteredList.stream()
                  .collect(
                      Collectors.toConcurrentMap(
                          KwKafkaConnector::getConnectorName, Function.identity(), (p, q) -> p))
                  .values());
    }

    connectorsFromSOT =
        connectorsFilteredList.stream()
            .sorted(new TopicNameComparator())
            .collect(Collectors.toList());

    return getConnectorModelsList(
        connectorsFromSOT, pageNo, currentPage, listAllEnvs, orderOfEnvs, tenantId);
  }

  private List<KafkaConnectorModelResponse> getConnectorModelsList(
      List<KwKafkaConnector> connectorsFromSOT,
      String pageNo,
      String currentPage,
      List<Env> listAllEnvs,
      String orderOfEnvs,
      int tenantId) {
    UserInfo user = manageDatabase.getHandleDbRequests().getUsersInfo(getUserName());

    int totalRecs = connectorsFromSOT.size();
    int recsPerPage = 21;

    int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);
    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);

    List<KafkaConnectorModelResponse> topicsListMap = null;
    if (totalRecs > 0) {
      topicsListMap = new ArrayList<>();
    }

    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < connectorsFromSOT.size(); i++) {
      int counterInc = counterIncrement();
      if (i >= startVar && i < lastVar) {
        KafkaConnectorModelResponse kafkaConnectorModelResponse = new KafkaConnectorModelResponse();
        kafkaConnectorModelResponse.setSequence(counterInc);
        KwKafkaConnector connectorSOT = connectorsFromSOT.get(i);

        List<String> envList = connectorSOT.getEnvironmentsList();
        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        kafkaConnectorModelResponse.setConnectorId(connectorSOT.getConnectorId());
        kafkaConnectorModelResponse.setEnvironmentId(connectorSOT.getEnvironment());
        kafkaConnectorModelResponse.setEnvironmentsList(
            KlawResourceUtils.getConvertedEnvs(listAllEnvs, envList));
        kafkaConnectorModelResponse.setConnectorName(connectorSOT.getConnectorName());
        kafkaConnectorModelResponse.setTeamName(
            manageDatabase.getTeamNameFromTeamId(tenantId, connectorSOT.getTeamId()));
        kafkaConnectorModelResponse.setTeamId(connectorSOT.getTeamId());

        kafkaConnectorModelResponse.setDescription(connectorSOT.getDescription());

        kafkaConnectorModelResponse.setTotalNoPages(totalPages + "");
        kafkaConnectorModelResponse.setCurrentPage(pageNo);

        kafkaConnectorModelResponse.setAllPageNos(numList);

        setKafkaConnectorBooleans(tenantId, user, kafkaConnectorModelResponse, connectorSOT);
        if (topicsListMap != null) {
          topicsListMap.add(kafkaConnectorModelResponse);
        }
      }
    }

    return topicsListMap;
  }

  private void setKafkaConnectorBooleans(
      int tenantId,
      UserInfo user,
      KafkaConnectorModelResponse connectorInfo,
      KwKafkaConnector connectorSOT) {
    connectorInfo.setConnectorOwner(connectorSOT.getTeamId() == user.getTeamId());

    connectorInfo.setHighestEnv(
        checkIsHighestEnv(connectorInfo.getEnvironmentId(), connectorInfo.getEnvironmentsList()));

    connectorInfo.setHasOpenRequest(
        isConnectorRequestOpen(
            tenantId, connectorSOT.getConnectorName(), connectorSOT.getEnvironment()));
  }

  private boolean isConnectorRequestOpen(int tenantId, String connectorName, String environment) {

    return manageDatabase
        .getHandleDbRequests()
        .existsConnectorRequest(connectorName, RequestStatus.CREATED.value, environment, tenantId);
  }

  private int topicCounter = 0;

  private int counterIncrement() {
    topicCounter++;
    return topicCounter;
  }

  static class TopicNameComparator implements Comparator<KwKafkaConnector> {
    @Override
    public int compare(KwKafkaConnector topic1, KwKafkaConnector topic2) {
      return topic1.getConnectorName().compareTo(topic2.getConnectorName());
    }
  }

  private List<KwKafkaConnector> groupConnectorsByEnv(List<KwKafkaConnector> topicsFromSOT) {
    List<KwKafkaConnector> tmpTopicList = new ArrayList<>();

    Map<String, List<KwKafkaConnector>> groupedList =
        topicsFromSOT.stream().collect(Collectors.groupingBy(KwKafkaConnector::getConnectorName));
    groupedList.forEach(
        (k, v) -> {
          KwKafkaConnector t = v.get(0);
          List<String> tmpEnvList = new ArrayList<>();
          for (KwKafkaConnector topic : v) {
            tmpEnvList.add(topic.getEnvironment());
          }
          t.setEnvironmentsList(tmpEnvList);
          tmpTopicList.add(t);
        });
    return tmpTopicList;
  }

  public ApiResponse deleteConnectorRequests(String topicId) throws KlawException {
    log.info("deleteConnectorRequests {}", topicId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_CONNECTORS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }
    try {
      String deleteTopicReqStatus =
          manageDatabase
              .getHandleDbRequests()
              .deleteConnectorRequest(
                  Integer.parseInt(topicId), commonUtilsService.getTenantId(getUserName()));

      return ApiResponse.builder().success(true).message(deleteTopicReqStatus).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse restartConnector(KafkaConnectorRestartModel kafkaConnectorRestartModel) {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    try {
      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.MANAGE_CONNECTORS)) {
        return ApiResponse.builder()
            .success(false)
            .message(ApiResultStatus.NOT_AUTHORIZED.value)
            .build();
      }
      return clusterApiService.restartConnector(kafkaConnectorRestartModel, tenantId);
    } catch (KlawException e) {
      return ApiResponse.builder()
          .success(false)
          .message("Connector could not be restarted")
          .build();
    }
  }

  public List<KafkaConnectorRequestsResponseModel> getCreatedConnectorRequests(
      String pageNo,
      String currentPage,
      String requestsType,
      String env,
      Order order,
      RequestOperationType requestOperationType,
      String search) {
    log.debug("getCreatedTopicRequests {} {}", pageNo, requestsType);
    String userDetails = getUserName();
    List<KafkaConnectorRequest> createdTopicReqList;
    int tenantId = commonUtilsService.getTenantId(getUserName());

    // get requests relevant to your teams or all teams
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS))
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedConnectorRequests(
                  userDetails, requestsType, false, tenantId, env, requestOperationType, search);
    else
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedConnectorRequests(
                  userDetails, requestsType, true, tenantId, env, requestOperationType, search);

    createdTopicReqList = filterByTenantAndOrder(userDetails, createdTopicReqList, order);
    createdTopicReqList = getConnectorRequestsPaged(createdTopicReqList, pageNo, currentPage);

    return updateCreateConnectorReqsList(createdTopicReqList, tenantId);
  }

  private List<KafkaConnectorRequestsResponseModel> updateCreateConnectorReqsList(
      List<KafkaConnectorRequest> kafkaConnectorRequests, int tenantId) {
    List<KafkaConnectorRequestsResponseModel> connectorRequestModels =
        getConnectorRequestModels(kafkaConnectorRequests);

    for (KafkaConnectorRequestsResponseModel kafkaConnectorRequestModel : connectorRequestModels) {
      kafkaConnectorRequestModel.setTeamname(
          manageDatabase.getTeamNameFromTeamId(tenantId, kafkaConnectorRequestModel.getTeamId()));
      kafkaConnectorRequestModel.setEnvironmentName(
          getKafkaConnectEnvDetails(kafkaConnectorRequestModel.getEnvironment()).getName());
    }

    return connectorRequestModels;
  }

  private String createConnectorConfig(KafkaConnectorRequest connectorRequest) {
    ConnectorConfig connectorConfig = new ConnectorConfig();
    connectorConfig.setName(connectorRequest.getConnectorName());

    try {
      JsonNode jsonNode = OBJECT_MAPPER.readTree(connectorRequest.getConnectorConfig());
      ObjectNode objectNode = (ObjectNode) jsonNode;
      connectorConfig.setConfig(objectNode);

      return WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(connectorConfig);
    } catch (JsonProcessingException e) {
      log.error("Exception:", e);
      return e.toString();
    }
  }

  public ApiResponse approveConnectorRequests(String connectorId)
      throws KlawException, KlawRestException {
    log.info("approveConnectorRequests {}", connectorId);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_CONNECTORS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    KafkaConnectorRequest connectorRequest =
        manageDatabase
            .getHandleDbRequests()
            .getConnectorRequestsForConnector(Integer.parseInt(connectorId), tenantId);

    String jsonConnectorConfig;

    try {
      jsonConnectorConfig = createConnectorConfig(connectorRequest);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(KAFKA_CONNECT_ERR_112);
    }

    if (connectorRequest.getRequestor().equals(userDetails)) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_113).build();
    }

    if (!RequestStatus.CREATED.value.equals(connectorRequest.getRequestStatus())) {
      return ApiResponse.builder().success(false).message(REQ_ERR_101).build();
    }

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());
    if (!allowedEnvIdSet.contains(connectorRequest.getEnvironment())) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    String updateConnectorReqStatus;
    if (RequestOperationType.CLAIM.value.equals(connectorRequest.getRequestOperationType())) {
      List<KwKafkaConnector> allConnectors =
          getConnectorsFromName(connectorRequest.getConnectorName(), tenantId);
      for (KwKafkaConnector connector : allConnectors) {
        connector.setTeamId(
            connectorRequest.getTeamId()); // for claim reqs, team stored in approving Team
        connector.setExistingConnector(true);
      }

      updateConnectorReqStatus = dbHandle.addToSyncConnectors(allConnectors);
      if (ApiResultStatus.SUCCESS.value.equals(updateConnectorReqStatus)) {
        updateConnectorReqStatus =
            dbHandle.updateConnectorRequestStatus(connectorRequest, userDetails);
      }
    } else {
      Env envSelected =
          manageDatabase
              .getHandleDbRequests()
              .getEnvDetails(connectorRequest.getEnvironment(), tenantId);
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA_CONNECT, tenantId)
              .get(envSelected.getClusterId());
      KafkaSupportedProtocol protocol = kwClusters.getProtocol();
      String kafkaConnectHost = kwClusters.getBootstrapServers();

      if (RequestOperationType.UPDATE.value.equals(
          connectorRequest.getRequestOperationType())) // only config
      {
        updateConnectorReqStatus =
            clusterApiService.approveConnectorRequests(
                connectorRequest.getConnectorName(),
                protocol,
                connectorRequest.getRequestOperationType(),
                connectorRequest.getConnectorConfig(),
                kafkaConnectHost,
                kwClusters.getClusterName() + kwClusters.getClusterId(),
                tenantId);
      } else {

        updateConnectorReqStatus =
            clusterApiService.approveConnectorRequests(
                connectorRequest.getConnectorName(),
                protocol,
                connectorRequest.getRequestOperationType(),
                jsonConnectorConfig,
                kafkaConnectHost,
                kwClusters.getClusterName() + kwClusters.getClusterId(),
                tenantId);
      }

      if (Objects.equals(updateConnectorReqStatus, ApiResultStatus.SUCCESS.value)) {
        setConnectorHistory(connectorRequest, userDetails, tenantId);
        updateConnectorReqStatus = dbHandle.updateConnectorRequest(connectorRequest, userDetails);
        mailService.sendMail(
            connectorRequest.getConnectorName(),
            null,
            "",
            connectorRequest.getRequestor(),
            connectorRequest.getApprover(),
            NumberUtils.toInt(connectorRequest.getApprovingTeamId(), -1),
            dbHandle,
            CONNECTOR_REQUEST_APPROVED,
            commonUtilsService.getLoginUrl());
      }
    }

    return ApiResponse.builder()
        .success(ApiResultStatus.SUCCESS.value.equalsIgnoreCase(updateConnectorReqStatus))
        .message(updateConnectorReqStatus)
        .build();
  }

  private void setConnectorHistory(
      KafkaConnectorRequest connectorRequest, String userName, int tenantId) {
    try {
      AtomicReference<String> existingHistory = new AtomicReference<>("");
      List<ResourceHistory> existingConnectorHistory;
      List<ResourceHistory> connectorHistoryList = new ArrayList<>();

      if (RequestOperationType.UPDATE.value.equals(connectorRequest.getRequestOperationType())) {
        List<KwKafkaConnector> existingConnectorList =
            getConnectorsFromName(connectorRequest.getConnectorName(), tenantId);
        existingConnectorList.stream()
            .filter(
                connector ->
                    Objects.equals(connector.getEnvironment(), connectorRequest.getEnvironment()))
            .findFirst()
            .ifPresent(a -> existingHistory.set(a.getHistory()));
        existingConnectorHistory = OBJECT_MAPPER.readValue(existingHistory.get(), VALUE_TYPE_REF);
        connectorHistoryList.addAll(existingConnectorHistory);
      }

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

      ResourceHistory connectorHistory = new ResourceHistory();
      connectorHistory.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, connectorRequest.getTeamId()));
      connectorHistory.setEnvironmentName(
          getKafkaConnectEnvDetails(connectorRequest.getEnvironment()).getName());
      connectorHistory.setRequestedBy(connectorRequest.getRequestor());
      connectorHistory.setRequestedTime(simpleDateFormat.format(connectorRequest.getRequesttime()));
      connectorHistory.setApprovedBy(userName);
      connectorHistory.setApprovedTime(simpleDateFormat.format(new Date()));
      connectorHistory.setRemarks("Connector " + connectorRequest.getRequestOperationType());
      connectorHistoryList.add(connectorHistory);

      connectorRequest.setHistory(OBJECT_MAPPER.writer().writeValueAsString(connectorHistoryList));
    } catch (Exception e) {
      log.error("setConnectorHistory Docs ", e);
    }
  }

  public ApiResponse declineConnectorRequests(String connectorId, String reasonForDecline)
      throws KlawException {
    log.info("declineConnectorRequests {} {}", connectorId, reasonForDecline);
    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_CONNECTORS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    KafkaConnectorRequest connectorRequest =
        dbHandle.getConnectorRequestsForConnector(Integer.parseInt(connectorId), tenantId);

    if (!RequestStatus.CREATED.value.equals(connectorRequest.getRequestStatus())) {
      return ApiResponse.builder().success(false).message(REQ_ERR_101).build();
    }

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());
    if (!allowedEnvIdSet.contains(connectorRequest.getEnvironment())) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    try {
      String result = dbHandle.declineConnectorRequest(connectorRequest, userDetails);
      mailService.sendMail(
          connectorRequest.getConnectorName(),
          null,
          reasonForDecline,
          connectorRequest.getRequestor(),
          connectorRequest.getApprover(),
          NumberUtils.toInt(connectorRequest.getApprovingTeamId(), -1),
          dbHandle,
          CONNECTOR_REQUEST_DENIED,
          commonUtilsService.getLoginUrl());

      return ApiResponse.builder()
          .success((result.equals(ApiResultStatus.SUCCESS.value)))
          .message(result)
          .build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
  }

  // create a request to delete connector.
  public ApiResponse createConnectorDeleteRequest(String connectorName, String envId)
      throws KlawException {
    log.info("createConnectorDeleteRequest {} {}", connectorName, envId);
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_CONNECTORS)) {
      return ApiResponse.builder().message(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    KafkaConnectorRequest kafkaConnectorRequest = new KafkaConnectorRequest();
    List<KwKafkaConnector> connectors = getConnectorsFromName(connectorName, tenantId);

    Integer userTeamId = commonUtilsService.getTeamId(userDetails);
    if (connectors != null
        && connectors.size() > 0
        && !Objects.equals(connectors.get(0).getTeamId(), userTeamId)) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_114).build();
    }

    kafkaConnectorRequest.setRequestor(userDetails);
    kafkaConnectorRequest.setTeamId(userTeamId);
    kafkaConnectorRequest.setEnvironment(envId);
    kafkaConnectorRequest.setConnectorName(connectorName);
    kafkaConnectorRequest.setRequestOperationType(RequestOperationType.DELETE.value);
    kafkaConnectorRequest.setTenantId(tenantId);

    Optional<KwKafkaConnector> connectorOptional =
        getConnectorsFromName(connectorName, tenantId).stream()
            .filter(
                connector ->
                    Objects.equals(
                        connector.getEnvironment(), kafkaConnectorRequest.getEnvironment()))
            .findFirst();

    if (manageDatabase
            .getHandleDbRequests()
            .getConnectorRequests(
                kafkaConnectorRequest.getConnectorName(),
                kafkaConnectorRequest.getEnvironment(),
                RequestStatus.CREATED.value,
                tenantId)
            .size()
        > 0) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_115).build();
    }

    if (connectorOptional.isPresent()) {

      kafkaConnectorRequest.setConnectorConfig(connectorOptional.get().getConnectorConfig());
      mailService.sendMail(
          kafkaConnectorRequest.getConnectorName(),
          null,
          "",
          userDetails,
          kafkaConnectorRequest.getApprover(),
          NumberUtils.toInt(kafkaConnectorRequest.getApprovingTeamId(), -1),
          dbHandle,
          CONNECTOR_DELETE_REQUESTED,
          commonUtilsService.getLoginUrl());

      try {
        String result =
            manageDatabase
                .getHandleDbRequests()
                .requestForConnector(kafkaConnectorRequest)
                .get("result");
        return ApiResponse.builder()
            .success(result.equals(ApiResultStatus.SUCCESS.value))
            .message(result)
            .build();
      } catch (HttpServerErrorException | HttpClientErrorException e) {
        log.error("deleteConnectorRequests {} {}", connectorName, e.getMessage());

        return processRestErrorResponse(e, CLUSTER_API_ERR_118);

      } catch (Exception e) {
        log.error(e.getMessage());
        throw new KlawException(e.getMessage());
      }
    } else {
      log.error("Connector not found : {}", connectorName);
      return ApiResponse.builder()
          .success(false)
          .message(String.format(KAFKA_CONNECT_ERR_116, connectorName))
          .build();
    }
  }

  private ApiResponse processRestErrorResponse(HttpStatusCodeException e, String defaultMsg) {
    RestErrorResponse errorResponse = null;
    errorResponse = e.getResponseBodyAs(RestErrorResponse.class);
    try {
      return ApiResponse.builder().success(false).message(errorResponse.getMessage()).build();
    } catch (Exception ex) {
      return ApiResponse.builder().success(false).message(defaultMsg).build();
    }
  }

  private boolean checkInPromotionOrder(String envId, String orderOfEnvs) {
    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
    return orderedEnv.contains(envId);
  }

  public List<KafkaConnectorRequestsResponseModel> getConnectorRequests(
      String pageNo,
      String currentPage,
      RequestStatus requestsType,
      RequestOperationType requestOperationType,
      String env,
      Order order,
      String search,
      boolean isMyRequest) {
    log.debug("getConnectorRequests page {} requestsType {}", pageNo, requestsType);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<KafkaConnectorRequest> kafkaConnectorRequests =
        manageDatabase
            .getHandleDbRequests()
            .getAllConnectorRequests(
                userDetails,
                requestOperationType,
                requestsType,
                env,
                search,
                tenantId,
                isMyRequest);

    kafkaConnectorRequests = filterByTenantAndOrder(userDetails, kafkaConnectorRequests, order);

    kafkaConnectorRequests = getConnectorRequestsPaged(kafkaConnectorRequests, pageNo, currentPage);

    return getConnectorRequestModels(kafkaConnectorRequests);
  }

  private List<KafkaConnectorRequest> filterByTenantAndOrder(
      String userDetails, List<KafkaConnectorRequest> connectorReqs, Order order) {
    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userDetails);
    try {
      connectorReqs =
          connectorReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .sorted(getPreferredOrder(order))
              .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return connectorReqs;
  }

  public ApiResponse createClaimConnectorRequest(String connectorName, String envId)
      throws KlawException {
    log.info("createClaimConnectorRequest {}", connectorName);
    String userDetails = getUserName();

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    KafkaConnectorRequest connectorRequest = new KafkaConnectorRequest();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (manageDatabase
            .getHandleDbRequests()
            .getConnectorRequests(connectorName, envId, RequestStatus.CREATED.value, tenantId)
            .size()
        > 0) {
      return ApiResponse.builder().success(false).message(KAFKA_CONNECT_ERR_117).build();
    }

    List<KwKafkaConnector> connectors = getConnectorsFromName(connectorName, tenantId);
    Integer connectorOwnerTeam = connectors.get(0).getTeamId();
    Optional<UserInfo> connectorOwnerContact =
        manageDatabase.getHandleDbRequests().getAllUsersInfo(tenantId).stream()
            .filter(user -> Objects.equals(user.getTeamId(), connectorOwnerTeam))
            .findFirst();

    Integer userTeamId = commonUtilsService.getTeamId(userDetails);

    connectorRequest.setRequestor(userDetails);
    connectorRequest.setTeamId(userTeamId);
    connectorRequest.setEnvironment(envId);
    connectorRequest.setConnectorName(connectorName);
    connectorRequest.setConnectorConfig(connectors.get(0).getConnectorConfig());
    connectorRequest.setRequestOperationType(RequestOperationType.CLAIM.value);
    connectorRequest.setApprovingTeamId("" + connectorOwnerTeam);
    connectorRequest.setRemarks(KAFKA_CONNECT_ERR_118);
    connectorRequest.setTenantId(tenantId);

    String approverName = null;
    Integer approverTeamId = null;
    if (connectorOwnerContact.isPresent()) {
      approverName = connectorOwnerContact.get().getUsername();
      approverTeamId = connectorOwnerContact.get().getTeamId();
    }
    mailService.sendMail(
        connectorRequest.getConnectorName(),
        null,
        "",
        userDetails,
        approverName,
        approverTeamId,
        dbHandle,
        CONNECTOR_CLAIM_REQUESTED,
        commonUtilsService.getLoginUrl());

    try {
      String res =
          manageDatabase.getHandleDbRequests().requestForConnector(connectorRequest).get("result");
      return ApiResponse.builder()
          .success(res.equals(ApiResultStatus.SUCCESS.value))
          .message(res)
          .build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
  }

  public ConnectorOverview getConnectorOverview(String connectorNamesearch, String envId) {
    log.debug("getConnectorOverview {}", connectorNamesearch);
    String userName = getUserName();
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();

    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (connectorNamesearch != null) {
      connectorNamesearch = connectorNamesearch.trim();
    } else {
      return null;
    }

    Integer loggedInUserTeam = commonUtilsService.getTeamId(userName);

    List<KwKafkaConnector> connectors = handleDb.getConnectors(connectorNamesearch, tenantId);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);

    connectors =
        connectors.stream()
            .filter(connectorObj -> allowedEnvIdSet.contains(connectorObj.getEnvironment()))
            .collect(Collectors.toList());

    ConnectorOverview connectorOverview = filterByEnvironment(connectors, envId, tenantId);

    if (connectors.size() == 0) {
      connectorOverview.setConnectorExists(false);
      return connectorOverview;
    } else {
      connectorOverview.setConnectorExists(true);
    }

    String syncCluster;
    String[] reqconnsEnvs;
    ArrayList<String> reqconnsEnvsList = new ArrayList<>();

    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncKafkaConnectCluster();
    } catch (Exception e) {
      log.error("Exception", e);
      syncCluster = null;
    }

    try {
      String requestconnsEnvs =
          commonUtilsService.getEnvProperty(tenantId, "REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS");
      reqconnsEnvs = requestconnsEnvs.split(",");
      reqconnsEnvsList = new ArrayList<>(Arrays.asList(reqconnsEnvs));
    } catch (Exception e) {
      log.error("Error in getting req Connector envs", e);
    }

    List<KafkaConnectorModelResponse> connectorInfoList = new ArrayList<>();
    ArrayList<ResourceHistory> connectorHistory;
    List<ResourceHistory> connectorHistoryList = new ArrayList<>();

    for (KwKafkaConnector conn : connectors) {
      if (StringUtils.isEmpty(envId) || conn.getEnvironment().equals(envId)) {
        KafkaConnectorModelResponse connectorInfo = new KafkaConnectorModelResponse();
        connectorInfo.setConnectorId(conn.getConnectorId());

        connectorInfo.setConnectorName(conn.getConnectorName());
        connectorInfo.setEnvironmentName(
            getKafkaConnectEnvDetails(conn.getEnvironment()).getName());
        connectorInfo.setEnvironmentId(conn.getEnvironment());
        connectorInfo.setConnectorConfig(conn.getConnectorConfig());
        connectorInfo.setTeamName(manageDatabase.getTeamNameFromTeamId(tenantId, conn.getTeamId()));

        if (Objects.equals(syncCluster, conn.getEnvironment())) {
          connectorOverview.setConnectorDocumentation(conn.getDocumentation());
          connectorOverview.setConnectorIdForDocumentation(conn.getConnectorId());
        }

        if (conn.getHistory() != null) {
          try {
            connectorHistory = OBJECT_MAPPER.readValue(conn.getHistory(), VALUE_TYPE_REF);
            connectorHistoryList.addAll(connectorHistory);
          } catch (JsonProcessingException e) {
            log.error("Unable to parse connectorHistory", e);
          }
        }

        connectorInfoList.add(connectorInfo);
      }
    }

    if (connectorOverview.getConnectorIdForDocumentation() == null) {
      connectorOverview.setConnectorDocumentation(connectors.get(0).getDocumentation());
      connectorOverview.setConnectorIdForDocumentation(connectors.get(0).getConnectorId());
    }

    connectorOverview.setConnectorHistoryList(connectorHistoryList);
    // TODO is this needed can we just grab this from the connectors above? circa line 1019
    List<KwKafkaConnector> connectorsSearchList =
        manageDatabase.getHandleDbRequests().getConnectorsFromName(connectorNamesearch, tenantId);

    // tenant filtering
    Integer connectorOwnerTeam =
        getFilteredConnectorsForTenant(connectorsSearchList).get(0).getTeamId();

    for (KafkaConnectorModelResponse connectorInfo : connectorInfoList) {
      // show edit button only for restricted envs
      if (Objects.equals(connectorOwnerTeam, loggedInUserTeam)
          && reqconnsEnvsList.contains(connectorInfo.getEnvironmentId())) {
        connectorInfo.setShowEditConnector(true);
        connectorInfo.setConnectorOwner(true);
      }
    }

    connectorOverview.setConnectorInfoList(connectorInfoList);
    try {
      if (Objects.equals(connectorOwnerTeam, loggedInUserTeam)) {
        connectorOverview.setPromotionDetails(
            getConnectorPromotionEnv(connectorNamesearch, connectors, tenantId));

        if (connectorOverview.getPromotionDetails().get("status").equals("success")
            && !StringUtils.isEmpty(envId)) {
          if (!connectorOverview.getPromotionDetails().get("sourceEnv").equals(envId)) {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("status", PromotionStatusType.NO_PROMOTION.value);
            connectorOverview.setPromotionDetails(hashMap);
          }
        }

        if (connectorInfoList.size() > 0) {
          KafkaConnectorModelResponse lastItem =
              connectorInfoList.get(connectorInfoList.size() - 1);
          lastItem.setConnectorDeletable(true);
          lastItem.setShowDeleteConnector(true);
        }

      } else {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("status", ApiResultStatus.NOT_AUTHORIZED.value);
        connectorOverview.setPromotionDetails(hashMap);
      }

      // Check if request open && set the highestEnv
      connectorInfoList.forEach(
          info -> {
            info.setHasOpenRequest(
                isConnectorRequestOpen(tenantId, info.getConnectorName(), info.getEnvironmentId()));
            info.setHighestEnv(
                checkIsHighestEnv(
                    info.getEnvironmentId(), connectorOverview.getAvailableEnvironments()));
          });

    } catch (Exception e) {
      log.error("Exception:", e);
      Map<String, String> hashMap = new HashMap<>();
      hashMap.put("status", ApiResultStatus.NOT_AUTHORIZED.value);
      connectorOverview.setPromotionDetails(hashMap);
    }

    return connectorOverview;
  }

  private boolean checkIsHighestEnv(String envId, List<EnvIdInfo> availableEnvs) {
    if (availableEnvs != null && !availableEnvs.isEmpty()) {
      return Objects.equals(availableEnvs.get(availableEnvs.size() - 1).getId(), envId);
    } else {
      return false;
    }
  }

  private ConnectorOverview filterByEnvironment(
      List<KwKafkaConnector> connectors, String envId, int tenantId) {
    ConnectorOverview overview = new ConnectorOverview();
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_KAFKA_CONNECT_ENVS);
    List<String> orderOfEnvsArrayList = KlawResourceUtils.getOrderedEnvsList(orderOfEnvs);
    List<EnvIdInfo> availableEnvs = new ArrayList<>();
    List<EnvIdInfo> availableEnvsNotInPromotionOrder = new ArrayList<>();
    connectors.forEach(
        conn -> {
          EnvIdInfo envIdInfo = new EnvIdInfo();
          envIdInfo.setId(conn.getEnvironment());
          envIdInfo.setName(
              manageDatabase.getKafkaConnectEnvList(tenantId).stream()
                  .filter(env -> env.getId().equals(conn.getEnvironment()))
                  .map(Env::getName)
                  .findFirst()
                  .orElse("ENV_NOT_FOUND"));
          if (orderOfEnvsArrayList.contains(envIdInfo.getId())) {
            availableEnvs.add(envIdInfo);
          } else {
            availableEnvsNotInPromotionOrder.add(envIdInfo);
          }
        });
    availableEnvs.sort(
        Comparator.comparingInt(
            topicEnv -> Objects.requireNonNull(orderOfEnvs).indexOf(topicEnv.getId())));
    availableEnvs.addAll(availableEnvsNotInPromotionOrder);
    overview.setAvailableEnvironments(availableEnvs);

    return overview;
  }

  public ConnectorOverviewPerEnv getConnectorDetailsPerEnv(String envId, String connectorName) {
    ConnectorOverviewPerEnv connectorOverviewPerEnv = new ConnectorOverviewPerEnv();
    connectorOverviewPerEnv.setConnectorExists(false);
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    KafkaConnectorModelResponse connectorModel = new KafkaConnectorModelResponse();
    List<KwKafkaConnector> connectors =
        manageDatabase.getHandleDbRequests().getConnectors(connectorName, tenantId);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    connectors =
        connectors.stream()
            .filter(topicObj -> allowedEnvIdSet.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    String topicDescription = "";
    if (connectors.size() == 0) {
      connectorOverviewPerEnv.setError("Connector does not exist.");
      return connectorOverviewPerEnv;
    } else {
      Optional<KwKafkaConnector> topicDescFound =
          connectors.stream()
              .filter(
                  topic -> topic.getDescription() != null && topic.getDescription().length() > 0)
              .findFirst();
      if (topicDescFound.isPresent()) {
        topicDescription = topicDescFound.get().getDescription();
      }
      connectors =
          connectors.stream()
              .filter(topic -> Objects.equals(topic.getEnvironment(), envId))
              .collect(Collectors.toList());
    }

    for (KwKafkaConnector kafkaConnector : connectors) {
      connectorModel.setEnvironmentName(
          getKafkaConnectEnvDetails(kafkaConnector.getEnvironment()).getName());
      connectorModel.setEnvironmentId(kafkaConnector.getEnvironment());
      connectorModel.setConnectorConfig(kafkaConnector.getConnectorConfig());
      connectorModel.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, kafkaConnector.getTeamId()));
      connectorOverviewPerEnv.setConnectorId(kafkaConnector.getConnectorId());
    }

    connectorModel.setDescription(topicDescription);

    String loggedInUserTeam =
        manageDatabase
            .getHandleDbRequests()
            .getAllTeamsOfUsers(getUserName(), tenantId)
            .get(0)
            .getTeamname();
    if (!Objects.equals(loggedInUserTeam, connectorModel.getTeamName())) {
      connectorOverviewPerEnv.setError(KAFKA_CONNECT_ERR_119);
      return connectorOverviewPerEnv;
    }

    if (connectorModel.getConnectorConfig() != null) {
      connectorOverviewPerEnv.setConnectorExists(true);
      connectorOverviewPerEnv.setConnectorContents(connectorModel);
    }
    return connectorOverviewPerEnv;
  }

  private Map<String, String> getConnectorPromotionEnv(
      String topicSearch, List<KwKafkaConnector> kafkaConnectors, int tenantId) {
    Map<String, String> hashMap = new HashMap<>();
    try {
      if (kafkaConnectors == null)
        kafkaConnectors = manageDatabase.getHandleDbRequests().getConnectors(topicSearch, tenantId);

      hashMap.put("connectorName", topicSearch);

      if (kafkaConnectors != null && kafkaConnectors.size() > 0) {
        List<String> envList = new ArrayList<>();
        kafkaConnectors.forEach(topic -> envList.add(topic.getEnvironment()));

        // tenant filtering
        String orderOfEnvs =
            commonUtilsService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");
        if (orderOfEnvs.length() == 0) {
          // No promotion order set return no promotion
          hashMap.put("status", PromotionStatusType.NO_PROMOTION.value);
          return hashMap;
        }

        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        String lastEnv = envList.get(envList.size() - 1);
        AtomicReference<String> sourceConnectorConfig = new AtomicReference<>("");
        kafkaConnectors.stream()
            .filter(kwKafkaConnector -> Objects.equals(kwKafkaConnector.getEnvironment(), lastEnv))
            .findFirst()
            .ifPresent(a -> sourceConnectorConfig.set(a.getConnectorConfig()));

        List<String> orderdEnvs = Arrays.asList(orderOfEnvs.split(","));

        if (orderdEnvs.indexOf(lastEnv) == orderdEnvs.size() - 1) {
          hashMap.put("status", PromotionStatusType.NO_PROMOTION.value); // PRD
        } else {
          hashMap.put("status", ApiResultStatus.SUCCESS.value);
          hashMap.put("sourceEnv", lastEnv);
          hashMap.put("sourceConnectorConfig", sourceConnectorConfig.get());
          String targetEnv = orderdEnvs.get(orderdEnvs.indexOf(lastEnv) + 1);
          if (getKafkaConnectEnvDetails(targetEnv) != null) {
            hashMap.put("targetEnv", getKafkaConnectEnvDetails(targetEnv).getName());
          }
          hashMap.put("targetEnvId", targetEnv);
        }

        return hashMap;
      }
    } catch (Exception e) {
      log.error("getConnectorPromotionEnv ", e);
      hashMap.put("status", ApiResultStatus.FAILURE.value);
      hashMap.put("error", KAFKA_CONNECT_ERR_120);
    }

    return hashMap;
  }

  public ApiResponse saveConnectorDocumentation(KafkaConnectorModel topicInfo) {
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    KwKafkaConnector kwKafkaConnector = new KwKafkaConnector();
    kwKafkaConnector.setConnectorId(topicInfo.getConnectorId());
    kwKafkaConnector.setDocumentation(topicInfo.getDocumentation());
    kwKafkaConnector.setTenantId(tenantId);

    List<KwKafkaConnector> topicsSearchList =
        manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(
                topicInfo.getConnectorName(), commonUtilsService.getTenantId(userName));

    // tenant filtering
    Integer topicOwnerTeam = getFilteredConnectorsForTenant(topicsSearchList).get(0).getTeamId();
    Integer loggedInUserTeam = commonUtilsService.getTeamId(userName);

    if (Objects.equals(topicOwnerTeam, loggedInUserTeam)) {
      return ApiResponse.builder()
          .success(true)
          .message(
              manageDatabase.getHandleDbRequests().updateConnectorDocumentation(kwKafkaConnector))
          .build();
    } else {
      return ApiResponse.builder().success(false).message(ApiResultStatus.FAILURE.value).build();
    }
  }

  private List<KafkaConnectorRequestsResponseModel> getConnectorRequestModels(
      List<KafkaConnectorRequest> topicsList) {
    List<KafkaConnectorRequestsResponseModel> topicRequestModelList = new ArrayList<>();
    KafkaConnectorRequestsResponseModel kafkaConnectorRequestModel;
    String userName = getUserName();
    Integer userTeamId = commonUtilsService.getTeamId(userName);

    int tenantId = commonUtilsService.getTenantId(userName);
    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("CONNECTORS", tenantId);
    List<UserInfo> userList =
        manageDatabase.getHandleDbRequests().getAllUsersInfoForTeam(userTeamId, tenantId);

    for (KafkaConnectorRequest connectorRequest : topicsList) {
      kafkaConnectorRequestModel = new KafkaConnectorRequestsResponseModel();
      copyProperties(connectorRequest, kafkaConnectorRequestModel);
      kafkaConnectorRequestModel.setRequestStatus(
          RequestStatus.of(connectorRequest.getRequestStatus()));
      kafkaConnectorRequestModel.setRequestOperationType(
          RequestOperationType.of(connectorRequest.getRequestOperationType()));
      kafkaConnectorRequestModel.setTeamname(
          manageDatabase.getTeamNameFromTeamId(tenantId, kafkaConnectorRequestModel.getTeamId()));

      // show approving info only before approvals
      if (RequestStatus.APPROVED != kafkaConnectorRequestModel.getRequestStatus()) {
        if (kafkaConnectorRequestModel.getRequestOperationType() != null
            && RequestOperationType.CLAIM == kafkaConnectorRequestModel.getRequestOperationType()) {

          List<KwKafkaConnector> connectors =
              getConnectorsFromName(kafkaConnectorRequestModel.getConnectorName(), tenantId);
          if (!connectors.isEmpty()) {
            kafkaConnectorRequestModel.setApprovingTeamDetails(
                updateApproverInfo(
                    manageDatabase
                        .getHandleDbRequests()
                        .getAllUsersInfoForTeam(connectors.get(0).getTeamId(), tenantId),
                    manageDatabase.getTeamNameFromTeamId(tenantId, connectors.get(0).getTeamId()),
                    approverRoles,
                    kafkaConnectorRequestModel.getRequestor()));
          } else {
            log.warn(
                "Request Exists for Connector {} in env {} and Connector does not exist.",
                kafkaConnectorRequestModel.getConnectorName(),
                kafkaConnectorRequestModel.getEnvironmentName());
            kafkaConnectorRequestModel.setRemarks(
                "This Connector is not found in Klaw. Please contact your Administrator.");
          }
        } else
          kafkaConnectorRequestModel.setApprovingTeamDetails(
              updateApproverInfo(
                  userList,
                  manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                  approverRoles,
                  kafkaConnectorRequestModel.getRequestor()));
      }

      topicRequestModelList.add(setRequestorPermissions(kafkaConnectorRequestModel, userName));
    }
    return topicRequestModelList;
  }

  private KafkaConnectorRequestsResponseModel setRequestorPermissions(
      KafkaConnectorRequestsResponseModel req, String userName) {
    if (RequestStatus.CREATED == req.getRequestStatus()
        && userName != null
        && userName.equals(req.getRequestor())) {
      req.setDeletable(true);
      req.setEditable(true);
    }
    return req;
  }

  private String updateApproverInfo(
      List<UserInfo> userList, String teamName, List<String> approverRoles, String requestor) {
    StringBuilder approvingInfo = new StringBuilder("Team : " + teamName + ", Users : ");

    for (UserInfo userInfo : userList) {
      if (approverRoles.contains(userInfo.getRole())
          && !Objects.equals(requestor, userInfo.getUsername())) {
        approvingInfo.append(userInfo.getUsername()).append(",");
      }
    }

    return String.valueOf(approvingInfo);
  }

  private List<KafkaConnectorRequest> getConnectorRequestsPaged(
      List<KafkaConnectorRequest> origActivityList, String pageNo, String currentPage) {

    List<KafkaConnectorRequest> newList = new ArrayList<>();
    Env envSelected;

    if (origActivityList != null && origActivityList.size() > 0) {
      int totalRecs = origActivityList.size();
      int recsPerPage = 10;
      int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);

      pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
      int requestPageNo = Integer.parseInt(pageNo);
      int startVar = (requestPageNo - 1) * recsPerPage;
      int lastVar = (requestPageNo) * (recsPerPage);

      List<String> numList = new ArrayList<>();
      commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

      for (int i = 0; i < totalRecs; i++) {
        KafkaConnectorRequest activityLog = origActivityList.get(i);
        if (i >= startVar && i < lastVar) {
          activityLog.setAllPageNos(numList);
          activityLog.setTotalNoPages("" + totalPages);
          activityLog.setCurrentPage(pageNo);
          envSelected = getKafkaConnectEnvDetails(activityLog.getEnvironment());
          activityLog.setEnvironmentName(envSelected.getName());
          newList.add(activityLog);
        }
      }
    }

    return newList;
  }

  public List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId) {
    List<KwKafkaConnector> connectors =
        manageDatabase.getHandleDbRequests().getConnectorsFromName(connectorName, tenantId);

    // tenant filtering
    connectors = getFilteredConnectorsForTenant(connectors);

    return connectors;
  }

  private static Comparator<KafkaConnectorRequest> getPreferredOrder(Order order) {
    return switch (order) {
      case DESC_REQUESTED_TIME -> Collections.reverseOrder(
          Comparator.comparing(KafkaConnectorRequest::getRequesttime));
      case ASC_REQUESTED_TIME -> Comparator.comparing(KafkaConnectorRequest::getRequesttime);
    };
  }

  private List<KwKafkaConnector> getFilteredConnectorsForTenant(
      List<KwKafkaConnector> connectorsFromSOT) {
    // tenant filtering
    try {
      final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());
      if (connectorsFromSOT != null) {
        connectorsFromSOT =
            connectorsFromSOT.stream()
                .filter(connector -> allowedEnvIdSet.contains(connector.getEnvironment()))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return connectorsFromSOT;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public Env getKafkaConnectEnvDetails(String envId) {
    Optional<Env> envFound =
        manageDatabase
            .getKafkaConnectEnvList(commonUtilsService.getTenantId(getUserName()))
            .stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }
}
