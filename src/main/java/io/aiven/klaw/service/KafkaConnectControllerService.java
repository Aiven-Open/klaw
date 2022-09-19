package io.aiven.klaw.service;

import static io.aiven.klaw.model.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.*;
import io.aiven.klaw.model.connectorconfig.ConnectorConfig;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConnectControllerService {

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  public HashMap<String, String> createConnectorRequest(
      KafkaConnectorRequestModel topicRequestReq) {
    log.info("createConnectorRequest {}", topicRequestReq);
    String userDetails = getUserName();

    HashMap<String, String> hashMapTopicReqRes = new HashMap<>();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_CONNECTORS)) {
      hashMapTopicReqRes.put("result", "Not Authorized");
      return hashMapTopicReqRes;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      if (topicRequestReq.getConnectorConfig() != null
          && topicRequestReq.getConnectorConfig().trim().length() > 0) {
        JsonNode jsonNode = objectMapper.readTree(topicRequestReq.getConnectorConfig().trim());
        if (!jsonNode.has("tasks.max")) {
          hashMapTopicReqRes.put("result", "Failure. Invalid config. tasks.max is not configured");
          return hashMapTopicReqRes;
        } else if (!jsonNode.has("connector.class")) {
          hashMapTopicReqRes.put(
              "result", "Failure. Invalid config. connector.class is not configured");
          return hashMapTopicReqRes;
        } else if (!jsonNode.has("topic")) {
          hashMapTopicReqRes.put("result", "Failure. Invalid config. topic is not configured");
          return hashMapTopicReqRes;
        }

        Map<String, Object> resultMap =
            objectMapper.convertValue(jsonNode, new TypeReference<>() {});
        topicRequestReq.setConnectorConfig(
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap));
      }
    } catch (JsonProcessingException e) {
      log.error("Exception:", e);
      hashMapTopicReqRes.put("result", "Failure. Invalid config.");
      return hashMapTopicReqRes;
    }

    topicRequestReq.setRequestor(userDetails);
    topicRequestReq.setUsername(userDetails);

    Integer userTeamId = getMyTeamId(userDetails);

    topicRequestReq.setTeamId(userTeamId);
    String envSelected = topicRequestReq.getEnvironment();

    // tenant filtering
    if (!getEnvsFromUserId(userDetails).contains(envSelected)) {
      hashMapTopicReqRes.put(
          "result", "Failure. Not authorized to request connector for this environment.");
      return hashMapTopicReqRes;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    // tenant filtering
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String syncCluster;
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncKafkaConnectCluster();
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      return new HashMap<>();
    }
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");

    List<KwKafkaConnector> kafkaConnectorList =
        getConnectorsFromName(topicRequestReq.getConnectorName(), tenantId);

    if (kafkaConnectorList != null
        && kafkaConnectorList.size() > 0
        && !kafkaConnectorList.get(0).getTeamId().equals(topicRequestReq.getTeamId())) {
      hashMapTopicReqRes.put("result", "Failure. This connector is owned by a different team.");
      return hashMapTopicReqRes;
    }
    boolean promotionOrderCheck =
        checkInPromotionOrder(
            topicRequestReq.getConnectorName(), topicRequestReq.getEnvironment(), orderOfEnvs);

    if (kafkaConnectorList != null && kafkaConnectorList.size() > 0) {
      if (promotionOrderCheck) {
        int devTopicFound =
            (int)
                kafkaConnectorList.stream()
                    .filter(topic -> topic.getEnvironment().equals(syncCluster))
                    .count();
        if (devTopicFound != 1) {
          if (getKafkaConnectEnvDetails(syncCluster) == null) {
            hashMapTopicReqRes.put(
                "result", "Failure. This connector does not exist in base cluster");
          } else {
            hashMapTopicReqRes.put(
                "result",
                "Failure. This connector does not exist in "
                    + getKafkaConnectEnvDetails(syncCluster).getName()
                    + " cluster.");
          }
          return hashMapTopicReqRes;
        }
      }
    } else if (!topicRequestReq.getEnvironment().equals(syncCluster)) {
      if (promotionOrderCheck) {
        hashMapTopicReqRes.put(
            "result",
            "Failure. Please request for a connector first in "
                + getKafkaConnectEnvDetails(syncCluster).getName()
                + " cluster.");
        return hashMapTopicReqRes;
      }
    }

    if (kafkaConnectorList != null) {
      if (manageDatabase
              .getHandleDbRequests()
              .selectConnectorRequests(
                  topicRequestReq.getConnectorName(),
                  topicRequestReq.getEnvironment(),
                  RequestStatus.created.name(),
                  tenantId)
              .size()
          > 0) {
        hashMapTopicReqRes.put("result", "Failure. A connector request already exists.");
        return hashMapTopicReqRes;
      }
    }

    // Ignore connector exists check if Update request
    if (!topicRequestReq.getConnectortype().equals(TopicRequestTypes.Update.name())) {
      boolean topicExists = false;
      if (kafkaConnectorList != null) {
        topicExists =
            kafkaConnectorList.stream()
                .anyMatch(
                    topicEx -> topicEx.getEnvironment().equals(topicRequestReq.getEnvironment()));
      }
      if (topicExists) {
        hashMapTopicReqRes.put(
            "result", "Failure. This connector already exists in the selected cluster.");
        return hashMapTopicReqRes;
      }
    }

    KafkaConnectorRequest topicRequestDao = new KafkaConnectorRequest();
    copyProperties(topicRequestReq, topicRequestDao);
    topicRequestDao.setTenantId(tenantId);

    hashMapTopicReqRes.put("result", dbHandle.requestForConnector(topicRequestDao).get("result"));
    mailService.sendMail(
        topicRequestReq.getConnectorName(),
        null,
        "",
        userDetails,
        dbHandle,
        CONNECTOR_CREATE_REQUESTED,
        commonUtilsService.getLoginUrl());

    return hashMapTopicReqRes;
  }

  public List<List<KafkaConnectorModel>> getConnectors(
      String env, String pageNo, String currentPage, String connectorNameSearch, String teamName)
      throws Exception {
    log.debug("getConnectors {}", connectorNameSearch);
    List<KafkaConnectorModel> topicListUpdated =
        getConnectorsPaginated(env, pageNo, currentPage, connectorNameSearch, teamName);

    if (topicListUpdated != null && topicListUpdated.size() > 0) {
      updateTeamNamesForDisplay(topicListUpdated);
      return getPagedList(topicListUpdated);
    }

    return null;
  }

  private List<List<KafkaConnectorModel>> getPagedList(List<KafkaConnectorModel> topicsList) {

    List<List<KafkaConnectorModel>> newList = new ArrayList<>();
    List<KafkaConnectorModel> innerList = new ArrayList<>();
    int modulusFactor = 3;
    int i = 0;
    for (KafkaConnectorModel topicInfo : topicsList) {

      innerList.add(topicInfo);

      if (i % modulusFactor == (modulusFactor - 1)) {
        newList.add(innerList);
        innerList = new ArrayList<>();
      }
      i++;
    }

    if (innerList.size() > 0) newList.add(innerList);

    return newList;
  }

  private void updateTeamNamesForDisplay(List<KafkaConnectorModel> topicListUpdated) {
    topicListUpdated.forEach(
        topicInfo -> {
          if (topicInfo.getTeamName().length() > 9)
            topicInfo.setTeamName(topicInfo.getTeamName().substring(0, 8) + "...");
        });
  }

  private List<KafkaConnectorModel> getConnectorsPaginated(
      String env, String pageNo, String currentPage, String connectorNameSearch, String teamName) {
    if (connectorNameSearch != null) connectorNameSearch = connectorNameSearch.trim();

    int tenantId = commonUtilsService.getTenantId(getUserName());
    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();

    // Get Sync topics

    List<KwKafkaConnector> topicsFromSOT =
        handleDbRequests.getSyncConnectors(
            env, manageDatabase.getTeamIdFromTeamName(tenantId, teamName), tenantId);
    topicsFromSOT = getFilteredConnectorsForTenant(topicsFromSOT);
    List<Env> listAllEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);
    // tenant filtering

    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");
    topicsFromSOT = groupConnectorsByEnv(topicsFromSOT);

    List<KwKafkaConnector> topicFilteredList = topicsFromSOT;
    // Filter topics on topic name for search
    if (connectorNameSearch != null && connectorNameSearch.length() > 0) {
      final String topicSearchFilter = connectorNameSearch;
      topicFilteredList =
          topicsFromSOT.stream()
              .filter(topic -> topic.getConnectorName().contains(topicSearchFilter))
              .collect(Collectors.toList());

      // searching documentation
      List<KwKafkaConnector> searchDocList =
          topicsFromSOT.stream()
              .filter(
                  topic ->
                      (topic.getDocumentation() != null
                          && topic.getDocumentation().contains(topicSearchFilter)))
              .collect(Collectors.toList());

      topicFilteredList.addAll(searchDocList);
      topicFilteredList =
          new ArrayList<>(
              topicFilteredList.stream()
                  .collect(
                      Collectors.toConcurrentMap(
                          KwKafkaConnector::getConnectorName, Function.identity(), (p, q) -> p))
                  .values());
    }

    topicsFromSOT =
        topicFilteredList.stream().sorted(new TopicNameComparator()).collect(Collectors.toList());

    return getConnectorModelsList(
        topicsFromSOT, pageNo, currentPage, listAllEnvs, orderOfEnvs, tenantId);
  }

  private List<KafkaConnectorModel> getConnectorModelsList(
      List<KwKafkaConnector> topicsFromSOT,
      String pageNo,
      String currentPage,
      List<Env> listAllEnvs,
      String orderOfEnvs,
      int tenantId) {
    int totalRecs = topicsFromSOT.size();
    int recsPerPage = 21;

    int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);
    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);

    List<KafkaConnectorModel> topicsListMap = null;
    if (totalRecs > 0) topicsListMap = new ArrayList<>();

    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < topicsFromSOT.size(); i++) {
      int counterInc = counterIncrement();
      if (i >= startVar && i < lastVar) {
        KafkaConnectorModel mp = new KafkaConnectorModel();
        mp.setSequence(counterInc);
        KwKafkaConnector topicSOT = topicsFromSOT.get(i);

        List<String> envList = topicSOT.getEnvironmentsList();
        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        mp.setConnectorId(topicSOT.getConnectorId());
        mp.setEnvironmentId(topicSOT.getEnvironment());
        mp.setEnvironmentsList(getConvertedEnvs(listAllEnvs, envList));
        mp.setConnectorName(topicSOT.getConnectorName());
        mp.setTeamName(manageDatabase.getTeamNameFromTeamId(tenantId, topicSOT.getTeamId()));

        mp.setDescription(topicSOT.getDescription());

        mp.setTotalNoPages(totalPages + "");
        mp.setCurrentPage(pageNo);

        mp.setAllPageNos(numList);

        if (topicsListMap != null) {
          topicsListMap.add(mp);
        }
      }
    }

    return topicsListMap;
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

  public String deleteConnectorRequests(String topicId) {
    log.info("deleteConnectorRequests {}", topicId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_CONNECTORS)) {
      String res = "Not Authorized.";
      return "{\"result\":\"" + res + "\"}";
    }
    String deleteTopicReqStatus =
        manageDatabase
            .getHandleDbRequests()
            .deleteConnectorRequest(
                Integer.parseInt(topicId), commonUtilsService.getTenantId(getUserName()));

    return "{\"result\":\"" + deleteTopicReqStatus + "\"}";
  }

  public List<KafkaConnectorRequestModel> getCreatedConnectorRequests(
      String pageNo, String currentPage, String requestsType) {
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
              .getCreatedConnectorRequests(userDetails, requestsType, false, tenantId);
    else
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedConnectorRequests(userDetails, requestsType, true, tenantId);

    createdTopicReqList = getConnectorRequestsFilteredForTenant(createdTopicReqList);

    createdTopicReqList = getConnectorRequestsPaged(createdTopicReqList, pageNo, currentPage);

    return updateCreateConnectorReqsList(createdTopicReqList, tenantId);
  }

  private List<KafkaConnectorRequestModel> updateCreateConnectorReqsList(
      List<KafkaConnectorRequest> topicsList, int tenantId) {
    List<KafkaConnectorRequestModel> topicRequestModelList =
        getConnectorRequestModels(topicsList, true);

    for (KafkaConnectorRequestModel topicInfo : topicRequestModelList) {
      topicInfo.setTeamName(manageDatabase.getTeamNameFromTeamId(tenantId, topicInfo.getTeamId()));
      topicInfo.setEnvironmentName(getKafkaConnectEnvDetails(topicInfo.getEnvironment()).getName());
    }

    return topicRequestModelList;
  }

  private String createConnectorConfig(KafkaConnectorRequest connectorRequest) {
    ConnectorConfig connectorConfig = new ConnectorConfig();
    connectorConfig.setName(connectorRequest.getConnectorName());

    ObjectMapper om = new ObjectMapper();
    try {
      JsonNode jsonNode = om.readTree(connectorRequest.getConnectorConfig());
      ObjectNode objectNode = (ObjectNode) jsonNode;
      connectorConfig.setConfig(objectNode);

      return om.writerWithDefaultPrettyPrinter().writeValueAsString(connectorConfig);
    } catch (JsonProcessingException e) {
      log.error("Exception:", e);
      return e.toString();
    }
  }

  public HashMap<String, String> approveConnectorRequests(String connectorId) throws KlawException {
    log.info("approveConnectorRequests {}", connectorId);
    HashMap<String, String> resultMap = new HashMap<>();
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_CONNECTORS)) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }

    KafkaConnectorRequest connectorRequest =
        manageDatabase
            .getHandleDbRequests()
            .selectConnectorRequestsForConnector(Integer.parseInt(connectorId), tenantId);

    String jsonConnectorConfig;

    try {
      jsonConnectorConfig = createConnectorConfig(connectorRequest);
    } catch (Exception e) {
      log.error("Exception:", e);
      resultMap.put("result", "Failure " + e.getMessage());
      return resultMap;
    }

    if (connectorRequest.getRequestor().equals(userDetails)) {
      resultMap.put("result", "You are not allowed to approve your own connector requests.");
      return resultMap;
    }

    if (!connectorRequest.getConnectorStatus().equals(RequestStatus.created.name())) {
      resultMap.put("result", "This request does not exist anymore.");
      return resultMap;
    }

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    if (!allowedEnvIdList.contains(connectorRequest.getEnvironment())) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    String updateTopicReqStatus;
    if (connectorRequest.getConnectortype().equals(TopicRequestTypes.Claim.name())) {
      List<KwKafkaConnector> allTopics =
          getConnectorsFromName(connectorRequest.getConnectorName(), tenantId);
      for (KwKafkaConnector allTopic : allTopics) {
        allTopic.setTeamId(
            connectorRequest.getTeamId()); // for claim reqs, team stored in description
        allTopic.setExistingConnector(true);
      }

      updateTopicReqStatus = dbHandle.addToSyncConnectors(allTopics);
      if (updateTopicReqStatus.equals("success"))
        updateTopicReqStatus = dbHandle.updateConnectorRequestStatus(connectorRequest, userDetails);
    } else {
      Env envSelected =
          manageDatabase
              .getHandleDbRequests()
              .selectEnvDetails(connectorRequest.getEnvironment(), tenantId);
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA_CONNECT.value, tenantId)
              .get(envSelected.getClusterId());
      String protocol = kwClusters.getProtocol();
      String kafkaConnectHost = kwClusters.getBootstrapServers();

      if (connectorRequest.getConnectortype().equals("Update")) // only config
      updateTopicReqStatus =
            clusterApiService.approveConnectorRequests(
                connectorRequest.getConnectorName(),
                protocol,
                connectorRequest.getConnectortype(),
                connectorRequest.getConnectorConfig(),
                kafkaConnectHost,
                tenantId);
      else
        updateTopicReqStatus =
            clusterApiService.approveConnectorRequests(
                connectorRequest.getConnectorName(),
                protocol,
                connectorRequest.getConnectortype(),
                jsonConnectorConfig,
                kafkaConnectHost,
                tenantId);

      if (Objects.equals(updateTopicReqStatus, "success")) {
        setConnectorHistory(connectorRequest, userDetails, tenantId);
        updateTopicReqStatus = dbHandle.updateConnectorRequest(connectorRequest, userDetails);
        mailService.sendMail(
            connectorRequest.getConnectorName(),
            null,
            "",
            connectorRequest.getRequestor(),
            dbHandle,
            CONNECTOR_REQUEST_APPROVED,
            commonUtilsService.getLoginUrl());
      }
    }

    resultMap.put("result", updateTopicReqStatus);
    return resultMap;
  }

  private void setConnectorHistory(
      KafkaConnectorRequest connectorRequest, String userName, int tenantId) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      AtomicReference<String> existingHistory = new AtomicReference<>("");
      List<TopicHistory> existingTopicHistory;
      List<TopicHistory> topicHistoryList = new ArrayList<>();

      if (connectorRequest.getConnectortype().equals(TopicRequestTypes.Update.name())) {
        List<KwKafkaConnector> existingTopicList =
            getConnectorsFromName(connectorRequest.getConnectorName(), tenantId);
        existingTopicList.stream()
            .filter(topic -> topic.getEnvironment().equals(connectorRequest.getEnvironment()))
            .findFirst()
            .ifPresent(a -> existingHistory.set(a.getHistory()));
        existingTopicHistory = objectMapper.readValue(existingHistory.get(), ArrayList.class);
        topicHistoryList.addAll(existingTopicHistory);
      }

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

      TopicHistory topicHistory = new TopicHistory();
      topicHistory.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, connectorRequest.getTeamId()));
      topicHistory.setEnvironmentName(
          getKafkaConnectEnvDetails(connectorRequest.getEnvironment()).getName());
      topicHistory.setRequestedBy(connectorRequest.getRequestor());
      topicHistory.setRequestedTime(simpleDateFormat.format(connectorRequest.getRequesttime()));
      topicHistory.setApprovedBy(userName);
      topicHistory.setApprovedTime(simpleDateFormat.format(new Date()));
      topicHistory.setRemarks(connectorRequest.getConnectortype());
      topicHistoryList.add(topicHistory);

      connectorRequest.setHistory(objectMapper.writer().writeValueAsString(topicHistoryList));
    } catch (Exception e) {
      log.error("setTopicDocs ", e);
    }
  }

  public String declineConnectorRequests(String connectorId, String reasonForDecline) {
    log.info("declineConnectorRequests {} {}", connectorId, reasonForDecline);
    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_CONNECTORS))
      return "{\"result\":\"Not Authorized\"}";

    int tenantId = commonUtilsService.getTenantId(getUserName());

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    KafkaConnectorRequest connectorRequest =
        dbHandle.selectConnectorRequestsForConnector(Integer.parseInt(connectorId), tenantId);

    if (!connectorRequest.getConnectorStatus().equals(RequestStatus.created.name())) {
      return "{\"result\":\"This request does not exist anymore.\"}";
    }

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    if (!allowedEnvIdList.contains(connectorRequest.getEnvironment()))
      return "{\"result\":\"Not Authorized\"}";

    String result = dbHandle.declineConnectorRequest(connectorRequest, userDetails);
    mailService.sendMail(
        connectorRequest.getConnectorName(),
        null,
        reasonForDecline,
        connectorRequest.getRequestor(),
        dbHandle,
        CONNECTOR_REQUEST_DENIED,
        commonUtilsService.getLoginUrl());

    return "{\"result\":\"" + result + "\"}";
  }

  // create a request to delete connector.
  public HashMap<String, String> createConnectorDeleteRequest(String connectorName, String envId) {
    log.info("createConnectorDeleteRequest {} {}", connectorName, envId);
    String userDetails = getUserName();

    HashMap<String, String> hashMap = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_CONNECTORS)) {
      hashMap.put("result", "Not Authorized");
      return hashMap;
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    KafkaConnectorRequest topicRequestReq = new KafkaConnectorRequest();
    List<KwKafkaConnector> topics = getConnectorsFromName(connectorName, tenantId);

    Integer userTeamId = getMyTeamId(userDetails);
    if (topics != null && topics.size() > 0 && !topics.get(0).getTeamId().equals(userTeamId)) {
      hashMap.put(
          "result",
          "Failure. Sorry, you cannot delete this connector, as you are not part of this team.");
      return hashMap;
    }

    topicRequestReq.setRequestor(userDetails);
    topicRequestReq.setUsername(userDetails);
    topicRequestReq.setTeamId(userTeamId);
    topicRequestReq.setEnvironment(envId);
    topicRequestReq.setConnectorName(connectorName);
    topicRequestReq.setConnectortype(TopicRequestTypes.Delete.name());

    Optional<KwKafkaConnector> topicOb =
        getConnectorsFromName(connectorName, tenantId).stream()
            .filter(topic -> topic.getEnvironment().equals(topicRequestReq.getEnvironment()))
            .findFirst();

    if (manageDatabase
            .getHandleDbRequests()
            .selectConnectorRequests(
                topicRequestReq.getConnectorName(),
                topicRequestReq.getEnvironment(),
                RequestStatus.created.name(),
                tenantId)
            .size()
        > 0) {
      hashMap.put("result", "Failure. A delete connector request already exists.");
      return hashMap;
    }

    if (topicOb.isPresent()) {

      topicRequestReq.setConnectorConfig(topicOb.get().getConnectorConfig());
      mailService.sendMail(
          topicRequestReq.getConnectorName(),
          null,
          "",
          userDetails,
          dbHandle,
          CONNECTOR_DELETE_REQUESTED,
          commonUtilsService.getLoginUrl());

      hashMap.put(
          "result",
          manageDatabase.getHandleDbRequests().requestForConnector(topicRequestReq).get("result"));
    } else {
      log.error("Connector not found : {}", connectorName);
      hashMap.put("result", "failure");
    }

    return hashMap;
  }

  private boolean checkInPromotionOrder(String topicname, String envId, String orderOfEnvs) {
    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
    return orderedEnv.contains(envId);
  }

  public List<KafkaConnectorRequestModel> getConnectorRequests(
      String pageNo, String currentPage, String requestsType) {
    log.debug("getConnectorRequests page {} requestsType {}", pageNo, requestsType);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<KafkaConnectorRequest> topicReqs =
        manageDatabase.getHandleDbRequests().getAllConnectorRequests(userDetails, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(userDetails);
    topicReqs =
        topicReqs.stream()
            .filter(topicRequest -> allowedEnvIdList.contains(topicRequest.getEnvironment()))
            .sorted(
                Collections.reverseOrder(
                    Comparator.comparing(KafkaConnectorRequest::getRequesttime)))
            .collect(Collectors.toList());

    if (!requestsType.equals("all") && EnumUtils.isValidEnum(RequestStatus.class, requestsType))
      topicReqs =
          topicReqs.stream()
              .filter(topicRequest -> topicRequest.getConnectorStatus().equals(requestsType))
              .collect(Collectors.toList());

    topicReqs = getConnectorRequestsPaged(topicReqs, pageNo, currentPage);

    return getConnectorRequestModels(topicReqs, true);
  }

  public HashMap<String, String> createClaimConnectorRequest(String connectorName, String envId) {
    log.info("createClaimConnectorRequest {}", connectorName);
    String userDetails = getUserName();

    HashMap<String, String> resultMap = new HashMap<>();

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    KafkaConnectorRequest connectorRequest = new KafkaConnectorRequest();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (manageDatabase
            .getHandleDbRequests()
            .selectConnectorRequests(connectorName, envId, RequestStatus.created.name(), tenantId)
            .size()
        > 0) {
      resultMap.put("result", "Failure. A request already exists for this connector.");
      return resultMap;
    }

    List<KwKafkaConnector> topics = getConnectorsFromName(connectorName, tenantId);
    Integer topicOwnerTeam = topics.get(0).getTeamId();
    Optional<UserInfo> topicOwnerContact =
        manageDatabase.getHandleDbRequests().selectAllUsersInfo(tenantId).stream()
            .filter(user -> user.getTeamId().equals(topicOwnerTeam))
            .findFirst();

    Integer userTeamId = getMyTeamId(userDetails);

    connectorRequest.setRequestor(userDetails);
    connectorRequest.setUsername(userDetails);
    connectorRequest.setTeamId(userTeamId);
    connectorRequest.setEnvironment(envId);
    connectorRequest.setConnectorName(connectorName);
    connectorRequest.setConnectorConfig(topics.get(0).getConnectorConfig());
    connectorRequest.setConnectortype(TopicRequestTypes.Claim.name());
    connectorRequest.setDescription("" + topicOwnerTeam);
    connectorRequest.setRemarks("Connector Claim request for all available environments.");
    connectorRequest.setTenantId(tenantId);

    mailService.sendMail(
        connectorRequest.getConnectorName(),
        null,
        "",
        userDetails,
        dbHandle,
        CONNECTOR_CLAIM_REQUESTED,
        commonUtilsService.getLoginUrl());

    topicOwnerContact.ifPresent(
        userInfo ->
            mailService.sendMail(
                connectorRequest.getConnectorName(),
                null,
                "",
                userInfo.getUsername(),
                dbHandle,
                CONNECTOR_CLAIM_REQUESTED,
                commonUtilsService.getLoginUrl()));

    resultMap.put(
        "result",
        manageDatabase.getHandleDbRequests().requestForConnector(connectorRequest).get("result"));

    return resultMap;
  }

  public ConnectorOverview getConnectorOverview(String connectorNamesearch) {
    log.debug("getConnectorOverview {}", connectorNamesearch);
    String userDetails = getUserName();
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();

    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (connectorNamesearch != null) connectorNamesearch = connectorNamesearch.trim();
    else return null;

    Integer loggedInUserTeam = getMyTeamId(userDetails);

    List<KwKafkaConnector> connectors = handleDb.getConnectors(connectorNamesearch, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(userDetails);
    connectors =
        connectors.stream()
            .filter(topicObj -> allowedEnvIdList.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    ConnectorOverview topicOverview = new ConnectorOverview();

    if (connectors.size() == 0) {
      topicOverview.setConnectorExists(false);
      return topicOverview;
    } else topicOverview.setConnectorExists(true);

    String syncCluster;
    String[] reqTopicsEnvs;
    ArrayList<String> reqTopicsEnvsList = new ArrayList<>();

    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncKafkaConnectCluster();
    } catch (Exception e) {
      log.error("Exception", e);
      syncCluster = null;
    }

    try {
      String requestTopicsEnvs =
          mailService.getEnvProperty(tenantId, "REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS");
      reqTopicsEnvs = requestTopicsEnvs.split(",");
      reqTopicsEnvsList = new ArrayList<>(Arrays.asList(reqTopicsEnvs));
    } catch (Exception e) {
      log.error("Error in getting req topic envs", e);
    }

    List<KafkaConnectorModel> topicInfoList = new ArrayList<>();
    ArrayList<TopicHistory> topicHistoryFromTopic;
    List<TopicHistory> topicHistoryList = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();

    for (KwKafkaConnector topic : connectors) {
      KafkaConnectorModel topicInfo = new KafkaConnectorModel();
      topicInfo.setConnectorName(topic.getConnectorName());
      topicInfo.setEnvironmentName(getKafkaConnectEnvDetails(topic.getEnvironment()).getName());
      topicInfo.setEnvironmentId(topic.getEnvironment());
      topicInfo.setConnectorConfig(topic.getConnectorConfig());
      topicInfo.setTeamName(manageDatabase.getTeamNameFromTeamId(tenantId, topic.getTeamId()));

      if (syncCluster != null && syncCluster.equals(topic.getEnvironment())) {
        topicOverview.setTopicDocumentation(topic.getDocumentation());
        topicOverview.setTopicIdForDocumentation(topic.getConnectorId());
      }

      if (topic.getHistory() != null) {
        try {
          topicHistoryFromTopic = objectMapper.readValue(topic.getHistory(), ArrayList.class);
          topicHistoryList.addAll(topicHistoryFromTopic);
        } catch (JsonProcessingException e) {
          log.error("Unable to parse topicHistory", e);
        }
      }

      topicInfoList.add(topicInfo);
    }

    if (topicOverview.getTopicIdForDocumentation() == null) {
      topicOverview.setTopicDocumentation(connectors.get(0).getDocumentation());
      topicOverview.setTopicIdForDocumentation(connectors.get(0).getConnectorId());
    }

    topicOverview.setTopicHistoryList(topicHistoryList);

    List<KwKafkaConnector> topicsSearchList =
        manageDatabase.getHandleDbRequests().getConnectorsFromName(connectorNamesearch, tenantId);

    // tenant filtering
    Integer topicOwnerTeam = getFilteredConnectorsForTenant(topicsSearchList).get(0).getTeamId();

    for (KafkaConnectorModel topicInfo : topicInfoList) {
      // show edit button only for restricted envs
      if (topicOwnerTeam.equals(loggedInUserTeam)
          && reqTopicsEnvsList.contains(topicInfo.getEnvironmentId())) {
        topicInfo.setShowEditConnector(true);
      }
    }

    topicOverview.setTopicInfoList(topicInfoList);
    try {
      if (topicOwnerTeam.equals(loggedInUserTeam)) {
        topicOverview.setPromotionDetails(
            getConnectorPromotionEnv(connectorNamesearch, connectors, tenantId));

        if (topicInfoList.size() > 0) {
          KafkaConnectorModel lastItem = topicInfoList.get(topicInfoList.size() - 1);
          lastItem.setConnectorDeletable(true);
          lastItem.setShowDeleteConnector(true);
        }
      } else {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("status", "not_authorized");
        topicOverview.setPromotionDetails(hashMap);
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      HashMap<String, String> hashMap = new HashMap<>();
      hashMap.put("status", "not_authorized");
      topicOverview.setPromotionDetails(hashMap);
    }

    return topicOverview;
  }

  public HashMap<String, Object> getConnectorDetailsPerEnv(String envId, String connectorName) {
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("connectorExists", false);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    KafkaConnectorModel connectorModel = new KafkaConnectorModel();
    List<KwKafkaConnector> connectors =
        manageDatabase.getHandleDbRequests().getConnectors(connectorName, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    connectors =
        connectors.stream()
            .filter(topicObj -> allowedEnvIdList.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    String topicDescription = "";
    if (connectors.size() == 0) {
      hashMap.put("error", "Connector does not exist.");
      return hashMap;
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
              .filter(topic -> topic.getEnvironment().equals(envId))
              .collect(Collectors.toList());
    }

    for (KwKafkaConnector kafkaConnector : connectors) {
      connectorModel.setEnvironmentName(
          getKafkaConnectEnvDetails(kafkaConnector.getEnvironment()).getName());
      connectorModel.setEnvironmentId(kafkaConnector.getEnvironment());
      connectorModel.setConnectorConfig(kafkaConnector.getConnectorConfig());
      connectorModel.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, kafkaConnector.getTeamId()));
      hashMap.put("connectorId", "" + kafkaConnector.getConnectorId());
    }

    connectorModel.setDescription(topicDescription);

    String loggedInUserTeam =
        manageDatabase
            .getHandleDbRequests()
            .selectAllTeamsOfUsers(getUserName(), tenantId)
            .get(0)
            .getTeamname();
    if (!loggedInUserTeam.equals(connectorModel.getTeamName())) {
      hashMap.put("error", "Sorry, your team does not own the connector !!");
      return hashMap;
    }

    if (connectorModel.getConnectorConfig() != null) {
      hashMap.put("connectorExists", true);
      hashMap.put("connectorContents", connectorModel);
    }
    return hashMap;
  }

  private HashMap<String, String> getConnectorPromotionEnv(
      String topicSearch, List<KwKafkaConnector> kafkaConnectors, int tenantId) {
    HashMap<String, String> hashMap = new HashMap<>();
    try {
      if (kafkaConnectors == null)
        kafkaConnectors = manageDatabase.getHandleDbRequests().getConnectors(topicSearch, tenantId);

      hashMap.put("connectorName", topicSearch);

      if (kafkaConnectors != null && kafkaConnectors.size() > 0) {
        List<String> envList = new ArrayList<>();
        List<String> finalEnvList = envList;
        kafkaConnectors.forEach(topic -> finalEnvList.add(topic.getEnvironment()));
        envList = finalEnvList;

        // tenant filtering
        String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");

        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        String lastEnv = envList.get(envList.size() - 1);
        AtomicReference<String> sourceConnectorConfig = new AtomicReference<>("");
        kafkaConnectors.stream()
            .filter(kwKafkaConnector -> kwKafkaConnector.getEnvironment().equals(lastEnv))
            .findFirst()
            .ifPresent(a -> sourceConnectorConfig.set(a.getConnectorConfig()));

        List<String> orderdEnvs = Arrays.asList(orderOfEnvs.split(","));

        if (orderdEnvs.indexOf(lastEnv) == orderdEnvs.size() - 1) {
          hashMap.put("status", "NO_PROMOTION"); // PRD
        } else {
          hashMap.put("status", "success");
          hashMap.put("sourceEnv", lastEnv);
          hashMap.put("sourceConnectorConfig", sourceConnectorConfig.get());
          String targetEnv = orderdEnvs.get(orderdEnvs.indexOf(lastEnv) + 1);
          hashMap.put("targetEnv", getKafkaConnectEnvDetails(targetEnv).getName());
          hashMap.put("targetEnvId", targetEnv);
        }

        return hashMap;
      }
    } catch (Exception e) {
      log.error("getConnectorPromotionEnv ", e);
      hashMap.put("status", "failure");
      hashMap.put("error", "Connector does not exist in any environment.");
    }

    return hashMap;
  }

  public HashMap<String, String> saveConnectorDocumentation(KafkaConnectorModel topicInfo) {
    HashMap<String, String> saveResult = new HashMap<>();

    KwKafkaConnector topic = new KwKafkaConnector();
    topic.setConnectorId(topicInfo.getConnectorId());
    topic.setDocumentation(topicInfo.getDocumentation());

    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    List<KwKafkaConnector> topicsSearchList =
        manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(
                topicInfo.getConnectorName(), commonUtilsService.getTenantId(getUserName()));

    // tenant filtering
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Integer topicOwnerTeam = getFilteredConnectorsForTenant(topicsSearchList).get(0).getTeamId();

    Integer loggedInUserTeam = getMyTeamId(getUserName());

    if (topicOwnerTeam.equals(loggedInUserTeam)) {
      saveResult.put(
          "result", manageDatabase.getHandleDbRequests().updateConnectorDocumentation(topic));
    } else saveResult.put("result", "failure");

    return saveResult;
  }

  private List<KafkaConnectorRequestModel> getConnectorRequestModels(
      List<KafkaConnectorRequest> topicsList, boolean fromSyncTopics) {
    List<KafkaConnectorRequestModel> topicRequestModelList = new ArrayList<>();
    KafkaConnectorRequestModel topicRequestModel;
    Integer userTeamId = getMyTeamId(getUserName());

    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("CONNECTORS", tenantId);
    List<UserInfo> userList =
        manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(userTeamId, tenantId);

    for (KafkaConnectorRequest topicReq : topicsList) {
      topicRequestModel = new KafkaConnectorRequestModel();
      copyProperties(topicReq, topicRequestModel);
      topicRequestModel.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, topicRequestModel.getTeamId()));

      if (fromSyncTopics) {
        // show approving info only before approvals
        if (!topicRequestModel.getConnectorStatus().equals(RequestStatus.approved.name())) {
          if (topicRequestModel.getConnectortype() != null
              && topicRequestModel.getConnectortype().equals(TopicRequestTypes.Claim.name())) {
            List<KwKafkaConnector> topics =
                getConnectorsFromName(topicRequestModel.getConnectorName(), tenantId);
            topicRequestModel.setApprovingTeamDetails(
                updateApproverInfo(
                    manageDatabase
                        .getHandleDbRequests()
                        .selectAllUsersInfoForTeam(topics.get(0).getTeamId(), tenantId),
                    manageDatabase.getTeamNameFromTeamId(tenantId, topics.get(0).getTeamId()),
                    approverRoles,
                    topicRequestModel.getRequestor()));
          } else
            topicRequestModel.setApprovingTeamDetails(
                updateApproverInfo(
                    userList,
                    manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                    approverRoles,
                    topicRequestModel.getRequestor()));
        }
      }

      topicRequestModelList.add(topicRequestModel);
    }
    return topicRequestModelList;
  }

  private String updateApproverInfo(
      List<UserInfo> userList, String teamName, List<String> approverRoles, String requestor) {
    StringBuilder approvingInfo = new StringBuilder("Team : " + teamName + ", Users : ");

    for (UserInfo userInfo : userList) {
      if (approverRoles.contains(userInfo.getRole()) && !requestor.equals(userInfo.getUsername()))
        approvingInfo.append(userInfo.getUsername()).append(",");
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

  private List<KafkaConnectorRequest> getConnectorRequestsFilteredForTenant(
      List<KafkaConnectorRequest> createdTopicReqList) {
    // tenant filtering
    try {
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      if (createdTopicReqList != null)
        createdTopicReqList =
            createdTopicReqList.stream()
                .filter(topicRequest -> allowedEnvIdList.contains(topicRequest.getEnvironment()))
                .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return createdTopicReqList;
  }

  private List<KwKafkaConnector> getFilteredConnectorsForTenant(
      List<KwKafkaConnector> connectorsFromSOT) {
    // tenant filtering
    try {
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      if (connectorsFromSOT != null)
        connectorsFromSOT =
            connectorsFromSOT.stream()
                .filter(connector -> allowedEnvIdList.contains(connector.getEnvironment()))
                .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return connectorsFromSOT;
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public Env getKafkaConnectEnvDetails(String envId) {
    Optional<Env> envFound =
        manageDatabase
            .getKafkaConnectEnvList(commonUtilsService.getTenantId(getUserName()))
            .stream()
            .filter(env -> env.getId().equals(envId))
            .findFirst();
    return envFound.orElse(null);
  }

  private List<String> getEnvsFromUserId(String userDetails) {
    Integer userTeamId = getMyTeamId(userDetails);
    return manageDatabase.getTeamsAndAllowedEnvs(
        userTeamId, commonUtilsService.getTenantId(getUserName()));
  }

  private List<String> getConvertedEnvs(List<Env> allEnvs, List<String> selectedEnvs) {
    List<String> newEnvList = new ArrayList<>();
    for (String env : selectedEnvs) {
      for (Env env1 : allEnvs) {
        if (env.equals(env1.getId())) {
          newEnvList.add(env1.getName());
          break;
        }
      }
    }

    return newEnvList;
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }
}
