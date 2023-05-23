package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.REQ_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_108;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_109;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_110;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_111;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_112;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_113;
import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_114;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
import static io.aiven.klaw.model.enums.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.TopicConfigEntry;
import io.aiven.klaw.model.TopicConfiguration;
import io.aiven.klaw.model.TopicConfigurationRequest;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.TopicRequestModel;
import io.aiven.klaw.model.response.TopicConfig;
import io.aiven.klaw.model.response.TopicDetailsPerEnv;
import io.aiven.klaw.model.response.TopicRequestsResponseModel;
import io.aiven.klaw.model.response.TopicTeamResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TopicControllerService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final TypeReference<List<TopicHistory>> VALUE_TYPE_REF = new TypeReference<>() {};
  @Autowired private final ClusterApiService clusterApiService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private final MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  private int topicCounter = 0;

  TopicControllerService(ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public ApiResponse createTopicsCreateRequest(TopicRequestModel topicRequestReq)
      throws KlawException, KlawNotAuthorizedException {
    log.info("createTopicsCreateRequest {}", topicRequestReq);
    checkIsAuthorized(PermissionType.REQUEST_CREATE_TOPICS);
    return createTopicRequest(topicRequestReq);
  }

  private void checkIsAuthorized(PermissionType permission) throws KlawNotAuthorizedException {
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), permission)) {
      throw new KlawNotAuthorizedException(TOPICS_ERR_101);
    }
  }

  public ApiResponse createTopicsUpdateRequest(TopicRequestModel topicRequestReq)
      throws KlawException, KlawNotAuthorizedException {
    log.info("createTopicsUpdateRequest {}", topicRequestReq);
    // check if authorized user to edit topic request
    checkIsAuthorized(PermissionType.REQUEST_EDIT_TOPICS);
    return createTopicRequest(topicRequestReq);
  }

  private ApiResponse createTopicRequest(TopicRequestModel topicRequestReq) throws KlawException {
    String userName = getUserName();

    topicRequestReq.setRequestor(userName);
    topicRequestReq.setTeamId(commonUtilsService.getTeamId(userName));

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    TopicRequest topicRequestDao = new TopicRequest();
    copyProperties(topicRequestReq, topicRequestDao);
    topicRequestDao.setRequestOperationType(topicRequestReq.getRequestOperationType().value);

    mapAdvancedTopicConfiguration(topicRequestReq, topicRequestDao);
    topicRequestDao.setTenantId(commonUtilsService.getTenantId(userName));

    String result = dbHandle.requestForTopic(topicRequestDao).get("result");

    mailService.sendMail(
        topicRequestReq.getTopicname(),
        null,
        "",
        userName,
        null,
        NumberUtils.toInt(topicRequestReq.getApprovingTeamId(), -1),
        dbHandle,
        TOPIC_CREATE_REQUESTED,
        commonUtilsService.getLoginUrl());

    return ApiResponse.builder()
        .success((result.equals(ApiResultStatus.SUCCESS.value)))
        .message(result)
        .build();
  }

  private void mapAdvancedTopicConfiguration(
      TopicRequestModel topicRequestModel, TopicRequest topicRequestDao) throws KlawException {
    Map<String, String> topicConfigs = new HashMap<>();
    try {
      List<TopicConfigEntry> advancedTopicConfigEntries =
          topicRequestModel.getAdvancedTopicConfigEntries();
      if (null != advancedTopicConfigEntries) {
        for (TopicConfigEntry advancedTopicConfig : advancedTopicConfigEntries) {
          topicConfigs.put(
              advancedTopicConfig.getConfigKey(), advancedTopicConfig.getConfigValue());
        }
      }
      TopicConfigurationRequest topicConfigurationRequest =
          TopicConfigurationRequest.builder().advancedTopicConfiguration(topicConfigs).build();
      topicRequestDao.setJsonParams(OBJECT_MAPPER.writeValueAsString(topicConfigurationRequest));
    } catch (JsonProcessingException e) {
      log.error("Error in processing topic configs ", e);
      throw new KlawException(TOPICS_ERR_102);
    }
  }

  // create a request to delete topic.
  public ApiResponse createTopicDeleteRequest(
      String topicName, String envId, boolean deleteAssociatedSchema)
      throws KlawException, KlawNotAuthorizedException {
    log.info(
        "createTopicDeleteRequest topicName {} envId {} deleteAssociatedSchema {}",
        topicName,
        envId,
        deleteAssociatedSchema);
    String userName = getUserName();

    // check if authorized user to delete topic request
    checkIsAuthorized(PermissionType.REQUEST_DELETE_TOPICS);

    int tenantId = commonUtilsService.getTenantId(userName);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    // check if already a delete topic request exists
    if (!dbHandle
        .getTopicRequests(topicName, envId, RequestStatus.CREATED.value, tenantId)
        .isEmpty()) {
      return ApiResponse.builder().success(false).message(TOPICS_ERR_103).build();
    }

    List<Topic> topics = getTopicFromName(topicName, tenantId);

    // check if you are part of the same team to delete this request
    Integer userTeamId = commonUtilsService.getTeamId(userName);
    if (topics != null
        && !topics.isEmpty()
        && !Objects.equals(topics.get(0).getTeamId(), userTeamId)) {
      return ApiResponse.builder().success(false).message(TOPICS_ERR_104).build();
    }

    TopicRequest topicRequestReq = new TopicRequest();
    topicRequestReq.setRequestor(userName);
    topicRequestReq.setTeamId(userTeamId);
    topicRequestReq.setEnvironment(envId);
    topicRequestReq.setTopicname(topicName);
    topicRequestReq.setRequestOperationType(RequestOperationType.DELETE.value);
    topicRequestReq.setTenantId(tenantId);
    topicRequestReq.setDeleteAssociatedSchema(deleteAssociatedSchema);

    Optional<Topic> topicOb = Optional.empty();
    if (topics != null) {
      topicOb =
          topics.stream()
              .filter(
                  topic -> Objects.equals(topic.getEnvironment(), topicRequestReq.getEnvironment()))
              .findFirst();
    }
    if (topicOb.isPresent()) {
      // Check if any existing subscriptions for this topic
      List<Acl> acls =
          dbHandle.getSyncAcls(
              topicRequestReq.getEnvironment(), topicRequestReq.getTopicname(), tenantId);
      if (!acls.isEmpty()) {
        return ApiResponse.builder().success(false).message(TOPICS_ERR_105).build();
      }

      topicRequestReq.setTopicpartitions(topicOb.get().getNoOfPartitions());
      topicRequestReq.setReplicationfactor(topicOb.get().getNoOfReplicas());
      try {
        mailService.sendMail(
            topicRequestReq.getTopicname(),
            null,
            "",
            userName,
            null,
            NumberUtils.toInt(topicRequestReq.getApprovingTeamId(), -1),
            dbHandle,
            TOPIC_DELETE_REQUESTED,
            commonUtilsService.getLoginUrl());

        String result = dbHandle.requestForTopic(topicRequestReq).get("result");
        return ApiResponse.builder()
            .success((result.equals(ApiResultStatus.SUCCESS.value)))
            .message(result)
            .build();
      } catch (Exception e) {
        log.error("Error ", e);
        throw new KlawException(e.getMessage());
      }
    } else {
      log.error("Topic not found : {}", topicName);
      return ApiResponse.builder()
          .success(false)
          .message(String.format(TOPICS_ERR_106, topicName))
          .build();
    }
  }

  public ApiResponse createClaimTopicRequest(String topicName, String envId) throws KlawException {
    log.info("createClaimTopicRequest {}", topicName);
    String userName = getUserName();

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    TopicRequest topicRequestReq = new TopicRequest();
    int tenantId = commonUtilsService.getTenantId(userName);

    if (!dbHandle
        .getTopicRequests(topicName, envId, RequestStatus.CREATED.value, tenantId)
        .isEmpty()) {
      return ApiResponse.builder().success(false).message(TOPICS_ERR_107).build();
    }

    List<Topic> topics = getTopicFromName(topicName, tenantId);
    Integer topicOwnerTeamId = topics.get(0).getTeamId();
    Optional<UserInfo> topicOwnerContact =
        dbHandle.getAllUsersInfo(tenantId).stream()
            .filter(user -> Objects.equals(user.getTeamId(), topicOwnerTeamId))
            .findFirst();

    Integer userTeamId = commonUtilsService.getTeamId(userName);

    topicRequestReq.setRequestor(userName);
    topicRequestReq.setTeamId(userTeamId);
    topicRequestReq.setTenantId(tenantId);
    topicRequestReq.setEnvironment(envId);
    topicRequestReq.setTopicname(topicName);
    topicRequestReq.setRequestOperationType(RequestOperationType.CLAIM.value);
    topicRequestReq.setApprovingTeamId(topicOwnerTeamId + "");
    topicRequestReq.setRemarks(TOPICS_108);

    String approverName = null;
    Integer approverTeamId = null;
    if (topicOwnerContact.isPresent()) {
      approverName = topicOwnerContact.get().getUsername();
      approverTeamId = topicOwnerContact.get().getTeamId();
    }

    mailService.sendMail(
        topicRequestReq.getTopicname(),
        null,
        "",
        userName,
        approverName,
        approverTeamId,
        dbHandle,
        TOPIC_CLAIM_REQUESTED,
        commonUtilsService.getLoginUrl());

    try {
      String result = dbHandle.requestForTopic(topicRequestReq).get("result");
      return ApiResponse.builder()
          .success((result.equals(ApiResultStatus.SUCCESS.value)))
          .message(result)
          .build();
    } catch (Exception e) {
      log.error("Error ", e);
      throw new KlawException(e.getMessage());
    }
  }

  public List<TopicRequestsResponseModel> getTopicRequests(
      String pageNo,
      String currentPage,
      RequestOperationType requestOperationType,
      String requestsType,
      String env,
      String wildcardSearch,
      Order order,
      boolean isMyRequest) {
    log.debug("getTopicRequests page {} requestsType {}", pageNo, requestsType);
    String userName = getUserName();
    List<TopicRequest> topicReqs =
        manageDatabase
            .getHandleDbRequests()
            .getAllTopicRequests(
                userName,
                requestsType,
                requestOperationType,
                env,
                wildcardSearch,
                isMyRequest,
                commonUtilsService.getTenantId(userName));

    // tenant filtering
    topicReqs = filterByTenantAndSort(order, userName, topicReqs);

    topicReqs = getTopicRequestsPaged(topicReqs, pageNo, currentPage);
    return getTopicRequestModels(topicReqs);
  }

  private List<TopicRequest> filterByTenantAndSort(
      Order order, String userName, List<TopicRequest> topicReqs) {
    try {
      final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
      topicReqs =
          topicReqs.stream()
              .filter(topicRequest -> allowedEnvIdSet.contains(topicRequest.getEnvironment()))
              .sorted(getPreferredOrdering(order))
              .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return topicReqs;
  }

  private Comparator<TopicRequest> getPreferredOrdering(Order order) {
    return switch (order) {
      case ASC_REQUESTED_TIME -> compareByTime();
      case DESC_REQUESTED_TIME -> Collections.reverseOrder(compareByTime());
    };
  }

  private static Comparator<TopicRequest> compareByTime() {
    return Comparator.comparing(TopicRequest::getRequesttime);
  }

  private TopicRequestsResponseModel setRequestorPermissions(
      TopicRequestsResponseModel req, String userName) {
    log.debug(
        " My Topic Status {} and userName {} and userName {}",
        req.getRequestStatus(),
        userName,
        req.getRequestor());
    if (RequestStatus.CREATED == req.getRequestStatus()
        && userName != null
        && userName.equals(req.getRequestor())) {
      req.setDeletable(true);
      req.setEditable(true);
    }

    return req;
  }

  private List<TopicRequest> getTopicRequestsPaged(
      List<TopicRequest> origActivityList, String pageNo, String currentPage) {

    List<TopicRequest> newList = new ArrayList<>();
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
        TopicRequest activityLog = origActivityList.get(i);
        if (i >= startVar && i < lastVar) {
          activityLog.setAllPageNos(numList);
          activityLog.setTotalNoPages("" + totalPages);
          activityLog.setCurrentPage(pageNo);
          envSelected = getEnvDetails(activityLog.getEnvironment());
          activityLog.setEnvironmentName(envSelected.getName());
          newList.add(activityLog);
        }
      }
    }

    return newList;
  }

  public TopicTeamResponse getTopicTeamOnly(String topicName, AclPatternType patternType) {
    log.debug("getTopicTeamOnly {} {}", topicName, patternType);
    TopicTeamResponse topicTeamResponse = new TopicTeamResponse();
    topicTeamResponse.setStatus(false);
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    List<Topic> topics;

    if (AclPatternType.PREFIXED == patternType) {
      topics = manageDatabase.getHandleDbRequests().getAllTopics(tenantId);

      // tenant filtering
      Set<String> allowedEnvIdSet = new HashSet<>(commonUtilsService.getEnvsFromUserId(userName));
      List<Topic> allTopicsStartingWithPattern = new ArrayList<>();

      topics.stream()
          .filter(topicRequest -> allowedEnvIdSet.contains(topicRequest.getEnvironment()))
          .distinct()
          .forEach(
              topic -> {
                if (topic.getTopicname().startsWith(topicName)) {
                  allTopicsStartingWithPattern.add(topic);
                }
              });

      if (allTopicsStartingWithPattern.isEmpty()) {
        topicTeamResponse.setError(TOPICS_ERR_109);
        return topicTeamResponse;
      }

      Set<Integer> stringTeamsFound = new HashSet<>();
      allTopicsStartingWithPattern.forEach(top -> stringTeamsFound.add(top.getTeamId()));

      if (stringTeamsFound.size() > 1) {
        topicTeamResponse.setError(TOPICS_ERR_110);
      } else {
        int teamId = stringTeamsFound.iterator().next();
        topicTeamResponse.setTeam(manageDatabase.getTeamNameFromTeamId(tenantId, teamId));
        topicTeamResponse.setTeamId(teamId);
      }
    } else {
      topics = commonUtilsService.getTopicsForTopicName(topicName, tenantId);
      // tenant filtering
      topics = commonUtilsService.getFilteredTopicsForTenant(topics);

      if (!topics.isEmpty()) {
        topicTeamResponse.setTeam(
            manageDatabase.getTeamNameFromTeamId(tenantId, topics.get(0).getTeamId()));
        topicTeamResponse.setTeamId(topics.get(0).getTeamId());
        topicTeamResponse.setStatus(true);
      } else {
        topicTeamResponse.setError(TOPICS_ERR_111);
      }
    }
    return topicTeamResponse;
  }

  public List<TopicRequestsResponseModel> getTopicRequestsForApprover(
      String pageNo,
      String currentPage,
      String requestsType,
      Integer teamId,
      String env,
      RequestOperationType requestOperationType,
      String wildcardSearch,
      Order order) {
    if (log.isDebugEnabled()) {
      log.debug(
          "getCreatedTopicRequests {} {} {} {} {}",
          pageNo,
          requestsType,
          teamId,
          env,
          requestOperationType,
          wildcardSearch);
    }

    String userName = getUserName();
    List<TopicRequest> createdTopicReqList;
    int tenantId = commonUtilsService.getTenantId(userName);
    // get requests relevant to your teams or all teams
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS)) {
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedTopicRequests(
                  userName,
                  requestsType,
                  false,
                  tenantId,
                  teamId,
                  env,
                  requestOperationType,
                  wildcardSearch);
    } else {
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedTopicRequests(
                  userName,
                  requestsType,
                  true,
                  tenantId,
                  teamId,
                  env,
                  requestOperationType,
                  wildcardSearch);
    }

    createdTopicReqList = filterByTenantAndSort(order, userName, createdTopicReqList);
    createdTopicReqList = getTopicRequestsPaged(createdTopicReqList, pageNo, currentPage);

    return updateCreateTopicReqsList(createdTopicReqList, tenantId);
  }

  private List<TopicRequestsResponseModel> updateCreateTopicReqsList(
      List<TopicRequest> topicsList, int tenantId) {
    List<TopicRequestsResponseModel> topicRequestModelList = getTopicRequestModels(topicsList);

    for (TopicRequestsResponseModel topicInfo : topicRequestModelList) {
      topicInfo.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topicInfo.getTeamId()));
      topicInfo.setEnvironmentName(getEnvDetails(topicInfo.getEnvironment()).getName());
    }

    return topicRequestModelList;
  }

  private List<TopicRequestsResponseModel> getTopicRequestModels(List<TopicRequest> topicsList) {
    List<TopicRequestsResponseModel> topicRequestModelList = new ArrayList<>();
    TopicRequestsResponseModel topicRequestModel;
    String userName = getUserName();
    Integer userTeamId = commonUtilsService.getTeamId(userName);

    int tenantId = commonUtilsService.getTenantId(userName);
    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("TOPICS", tenantId);
    List<UserInfo> userList =
        manageDatabase.getHandleDbRequests().getAllUsersInfoForTeam(userTeamId, tenantId);

    for (TopicRequest topicReq : topicsList) {
      topicRequestModel = new TopicRequestsResponseModel();
      copyProperties(topicReq, topicRequestModel);
      topicRequestModel.setRequestStatus(RequestStatus.of(topicReq.getRequestStatus()));
      topicRequestModel.setRequestOperationType(
          RequestOperationType.of(topicReq.getRequestOperationType()));

      validateAndCopyTopicConfigs(topicReq, topicRequestModel);

      topicRequestModel.setTeamname(
          manageDatabase.getTeamNameFromTeamId(tenantId, topicReq.getTeamId()));

      // show approving info only before approvals
      if (RequestStatus.APPROVED != topicRequestModel.getRequestStatus()) {
        if (topicRequestModel.getRequestOperationType() != null
            && RequestOperationType.CLAIM == topicRequestModel.getRequestOperationType()) {
          List<Topic> topics = getTopicFromName(topicRequestModel.getTopicname(), tenantId);
          topicRequestModel.setApprovingTeamDetails(
              updateApproverInfo(
                  manageDatabase
                      .getHandleDbRequests()
                      .getAllUsersInfoForTeam(topics.get(0).getTeamId(), tenantId),
                  manageDatabase.getTeamNameFromTeamId(tenantId, topics.get(0).getTeamId()),
                  approverRoles,
                  topicRequestModel.getRequestor()));
        } else {
          topicRequestModel.setApprovingTeamDetails(
              updateApproverInfo(
                  userList,
                  manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                  approverRoles,
                  topicRequestModel.getRequestor()));
        }
      }

      topicRequestModelList.add(setRequestorPermissions(topicRequestModel, userName));
    }
    return topicRequestModelList;
  }

  private void validateAndCopyTopicConfigs(
      TopicRequest topicReq, TopicRequestsResponseModel topicRequestModel) {
    try {
      if (topicReq.getJsonParams() != null) {
        List<TopicConfigEntry> topicConfigEntryList = new ArrayList<>();
        TopicConfigurationRequest topicConfigurationRequest =
            OBJECT_MAPPER.readValue(topicReq.getJsonParams(), TopicConfigurationRequest.class);
        for (Map.Entry<String, String> entry :
            topicConfigurationRequest.getAdvancedTopicConfiguration().entrySet()) {
          topicConfigEntryList.add(
              TopicConfigEntry.builder()
                  .configKey(entry.getKey())
                  .configValue(entry.getValue())
                  .build());
        }
        topicRequestModel.setAdvancedTopicConfigEntries(topicConfigEntryList);
      }
    } catch (JsonProcessingException e) {
      // ignore this error while retrieving the requests
      log.error("Error in parsing topic configs ", e);
    }
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

  public ApiResponse deleteTopicRequests(String topicId) throws KlawException {
    log.info("deleteTopicRequests {}", topicId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_TOPICS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }
    String userName = getUserName();
    try {
      String deleteTopicReqStatus =
          manageDatabase
              .getHandleDbRequests()
              .deleteTopicRequest(
                  Integer.parseInt(topicId),
                  userName,
                  commonUtilsService.getTenantId(getUserName()));

      return ApiResponse.builder()
          .success((deleteTopicReqStatus.equals(ApiResultStatus.SUCCESS.value)))
          .message(deleteTopicReqStatus)
          .build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
  }

  /*
  - On approval, create,delete,update the topics on kafka cluster with api call
  - For claim topics, there are no kafka cluster operations
   */
  public ApiResponse approveTopicRequests(String topicId) throws KlawException {
    log.info("approveTopicRequests {}", topicId);
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_TOPICS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    TopicRequest topicRequest =
        manageDatabase
            .getHandleDbRequests()
            .getTopicRequestsForTopic(Integer.parseInt(topicId), tenantId);

    ApiResponse validationResponse = validateTopicRequest(topicRequest, userName);
    if (!validationResponse.isSuccess()) {
      return validationResponse;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    String updateTopicReqStatus;

    if (RequestOperationType.CLAIM.value.equals(topicRequest.getRequestOperationType())) {
      List<Topic> allTopics = getTopicFromName(topicRequest.getTopicname(), tenantId);
      for (Topic allTopic : allTopics) {
        allTopic.setTeamId(
            topicRequest.getTeamId()); // for claim reqs, team stored in approving team field
        allTopic.setExistingTopic(true);
      }

      updateTopicReqStatus = dbHandle.addToSynctopics(allTopics);
      if (ApiResultStatus.SUCCESS.value.equals(updateTopicReqStatus)) {
        updateTopicReqStatus = dbHandle.updateTopicRequestStatus(topicRequest, userName);
      }
    } else {
      Map<String, String> topicConfig = null;
      try {
        if (null != topicRequest.getJsonParams()) {
          topicConfig =
              OBJECT_MAPPER
                  .readValue(topicRequest.getJsonParams(), TopicConfigurationRequest.class)
                  .getAdvancedTopicConfiguration();
        }
      } catch (JsonProcessingException e) {
        // ignore this error while executing the req. should have been raised earlier in the
        // process.
        log.error("Error in parsing topic config ", e);
      }
      updateTopicReqStatus =
          invokeClusterApiForTopicRequest(userName, tenantId, topicRequest, dbHandle, topicConfig);
    }

    if (updateTopicReqStatus.equals(ApiResultStatus.SUCCESS.value)) {
      commonUtilsService.updateMetadata(tenantId, EntityType.TOPICS, MetadataOperationType.CREATE);
    }

    return ApiResponse.builder()
        .success((updateTopicReqStatus.equals(ApiResultStatus.SUCCESS.value)))
        .message(updateTopicReqStatus)
        .build();
  }

  private String invokeClusterApiForTopicRequest(
      String userName,
      int tenantId,
      TopicRequest topicRequest,
      HandleDbRequests dbHandle,
      Map<String, String> topicConfig)
      throws KlawException {
    String updateTopicReqStatus;
    ResponseEntity<ApiResponse> response =
        clusterApiService.approveTopicRequests(
            topicRequest.getTopicname(),
            topicRequest.getRequestOperationType(),
            topicRequest.getTopicpartitions(),
            topicRequest.getReplicationfactor(),
            topicRequest.getEnvironment(),
            topicConfig,
            tenantId,
            topicRequest.getDeleteAssociatedSchema());

    updateTopicReqStatus = Objects.requireNonNull(response.getBody()).getMessage();

    if (response.getBody().isSuccess()) {
      setTopicHistory(topicRequest, userName, tenantId);
      updateTopicReqStatus = dbHandle.updateTopicRequest(topicRequest, userName);
      mailService.sendMail(
          topicRequest.getTopicname(),
          null,
          "",
          topicRequest.getRequestor(),
          topicRequest.getApprover(),
          NumberUtils.toInt(topicRequest.getApprovingTeamId(), -1),
          dbHandle,
          TOPIC_REQUEST_APPROVED,
          commonUtilsService.getLoginUrl());
    }
    return updateTopicReqStatus;
  }

  private ApiResponse validateTopicRequest(TopicRequest topicRequest, String userName) {
    if (Objects.equals(topicRequest.getRequestor(), userName)) {
      return ApiResponse.builder().success(false).message(TOPICS_ERR_112).build();
    }

    if (!RequestStatus.CREATED.value.equals(topicRequest.getRequestStatus())) {
      return ApiResponse.builder().success(false).message(REQ_ERR_101).build();
    }

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    if (!allowedEnvIdSet.contains(topicRequest.getEnvironment())) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }
    return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
  }

  private void setTopicHistory(TopicRequest topicRequest, String userName, int tenantId) {
    try {
      AtomicReference<String> existingHistory = new AtomicReference<>("");
      List<TopicHistory> existingTopicHistory;
      List<TopicHistory> topicHistoryList = new ArrayList<>();

      if (RequestOperationType.UPDATE.value.equals(topicRequest.getRequestOperationType())) {
        List<Topic> existingTopicList =
            getTopicFromName(topicRequest.getTopicname(), topicRequest.getTenantId());
        existingTopicList.stream()
            .filter(topic -> Objects.equals(topic.getEnvironment(), topicRequest.getEnvironment()))
            .findFirst()
            .ifPresent(a -> existingHistory.set(a.getHistory()));
        existingTopicHistory = OBJECT_MAPPER.readValue(existingHistory.get(), VALUE_TYPE_REF);
        topicHistoryList.addAll(existingTopicHistory);
      }

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

      TopicHistory topicHistory = new TopicHistory();
      topicHistory.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, topicRequest.getTeamId()));
      topicHistory.setEnvironmentName(getEnvDetails(topicRequest.getEnvironment()).getName());
      topicHistory.setRequestedBy(topicRequest.getRequestor());
      topicHistory.setRequestedTime(simpleDateFormat.format(topicRequest.getRequesttime()));
      topicHistory.setApprovedBy(userName);
      topicHistory.setApprovedTime(simpleDateFormat.format(new Date()));
      topicHistory.setRemarks(topicRequest.getRequestOperationType());
      topicHistoryList.add(topicHistory);

      topicRequest.setHistory(OBJECT_MAPPER.writer().writeValueAsString(topicHistoryList));
    } catch (Exception e) {
      log.error("Exception: ", e);
    }
  }

  public ApiResponse declineTopicRequests(String topicId, String reasonForDecline)
      throws KlawException {
    log.info("declineTopicRequests {} {}", topicId, reasonForDecline);
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_TOPICS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    String userName = getUserName();
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    TopicRequest topicRequest =
        dbHandle.getTopicRequestsForTopic(
            Integer.parseInt(topicId), commonUtilsService.getTenantId(userName));

    if (!RequestStatus.CREATED.value.equals(topicRequest.getRequestStatus())) {
      return ApiResponse.builder().success(false).message(REQ_ERR_101).build();
    }

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    if (!allowedEnvIdSet.contains(topicRequest.getEnvironment())) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    try {
      String result = dbHandle.declineTopicRequest(topicRequest, userName);
      mailService.sendMail(
          topicRequest.getTopicname(),
          null,
          reasonForDecline,
          topicRequest.getRequestor(),
          topicRequest.getApprover(),
          NumberUtils.toInt(topicRequest.getApprovingTeamId(), -1),
          dbHandle,
          TOPIC_REQUEST_DENIED,
          commonUtilsService.getLoginUrl());

      return ApiResponse.builder()
          .success((result.equals(ApiResultStatus.SUCCESS.value)))
          .message(result)
          .build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public List<String> getAllTopics(boolean isMyTeamTopics, String envSelected) {
    log.debug("getAllTopics {}, envSelected {}", isMyTeamTopics, envSelected);
    String userName = getUserName();

    List<Topic> topicsFromSOT =
        commonUtilsService.getTopics(
            envSelected, null, commonUtilsService.getTenantId(getUserName()));

    // tenant filtering
    topicsFromSOT = commonUtilsService.getFilteredTopicsForTenant(topicsFromSOT);

    if (isMyTeamTopics) {
      Integer userTeamId = commonUtilsService.getTeamId(userName);
      topicsFromSOT =
          topicsFromSOT.stream()
              .filter(topic -> Objects.equals(topic.getTeamId(), userTeamId))
              .collect(Collectors.toList());
    }

    List<String> topicsList = new ArrayList<>();
    topicsFromSOT.forEach(topic -> topicsList.add(topic.getTopicname()));

    return topicsList.stream().distinct().collect(Collectors.toList());
  }

  public ApiResponse saveTopicDocumentation(TopicInfo topicInfo) throws KlawException {
    Topic topic = new Topic();
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    topic.setTenantId(tenantId);
    topic.setTopicid(topicInfo.getTopicid());
    topic.setDocumentation(topicInfo.getDocumentation());

    List<Topic> topicsSearchList =
        commonUtilsService.getTopicsForTopicName(topicInfo.getTopicName(), tenantId);

    try {
      // tenant filtering
      Integer topicOwnerTeamId =
          commonUtilsService.getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();
      Integer loggedInUserTeamId = commonUtilsService.getTeamId(userName);
      if (Objects.equals(topicOwnerTeamId, loggedInUserTeamId)) {
        return ApiResponse.builder()
            .success(true)
            .message(manageDatabase.getHandleDbRequests().updateTopicDocumentation(topic))
            .build();
      } else {
        return ApiResponse.builder().success(false).message(ApiResultStatus.FAILURE.value).build();
      }
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public TopicDetailsPerEnv getTopicDetailsPerEnv(String envId, String topicName) {
    TopicDetailsPerEnv topicDetailsPerEnv = new TopicDetailsPerEnv();
    topicDetailsPerEnv.setTopicExists(false);

    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    TopicInfo topicInfo = new TopicInfo();
    List<Topic> topics = commonUtilsService.getTopicsForTopicName(topicName, tenantId);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    topics =
        topics.stream()
            .filter(topicObj -> allowedEnvIdSet.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    String topicDescription = "";
    if (topics.isEmpty()) {
      topicDetailsPerEnv.setError(TOPICS_ERR_113);
      return topicDetailsPerEnv;
    } else {
      Optional<Topic> topicDescFound =
          topics.stream()
              .filter(
                  topic -> topic.getDescription() != null && topic.getDescription().length() > 0)
              .findFirst();
      if (topicDescFound.isPresent()) {
        topicDescription = topicDescFound.get().getDescription();
      }
      Optional<Topic> topicOptional =
          topics.stream()
              .filter(topic -> Objects.equals(topic.getEnvironment(), envId))
              .findFirst();
      if (topicOptional.isPresent()) {
        topicInfo.setNoOfPartitions(topicOptional.get().getNoOfPartitions());
        topicInfo.setNoOfReplicas(topicOptional.get().getNoOfReplicas());
        topicDetailsPerEnv.setTopicId("" + topicOptional.get().getTopicid());
        topicInfo.setDescription(topicDescription);

        Integer loggedInUserTeamId = commonUtilsService.getTeamId(userName);
        if (!Objects.equals(loggedInUserTeamId, topicOptional.get().getTeamId())) {
          topicDetailsPerEnv.setError(TOPICS_ERR_114);
          return topicDetailsPerEnv;
        }
      }
    }

    if (topicInfo.getNoOfPartitions() != null) {
      topicDetailsPerEnv.setTopicExists(true);
      topicDetailsPerEnv.setTopicContents(topicInfo);
    }
    return topicDetailsPerEnv;
  }

  public Map<String, String> getAdvancedTopicConfigs() {
    return TopicConfiguration.getTopicConfigurations();
  }

  static class TopicNameComparator implements Comparator<Topic> {
    @Override
    public int compare(Topic topic1, Topic topic2) {
      return topic1.getTopicname().compareTo(topic2.getTopicname());
    }
  }

  static class TopicNameSyncComparator implements Comparator<TopicConfig> {
    @Override
    public int compare(TopicConfig topic1, TopicConfig topic2) {
      return topic1.getTopicName().compareTo(topic2.getTopicName());
    }
  }

  public List<List<TopicInfo>> getTopics(
      String env,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      Integer teamId,
      String topicType) {
    log.debug("getTopics {}", topicNameSearch);
    String userName = getUserName();
    List<TopicInfo> topicListUpdated =
        getTopicsPaginated(
            env,
            pageNo,
            currentPage,
            topicNameSearch,
            teamId,
            topicType,
            commonUtilsService.getTenantId(userName));

    if (topicListUpdated != null && topicListUpdated.size() > 0) {
      updateTeamNamesForDisplay(topicListUpdated);
      return getPagedList(topicListUpdated);
    }

    return null;
  }

  private void updateTeamNamesForDisplay(List<TopicInfo> topicListUpdated) {
    topicListUpdated.forEach(
        topicInfo -> {
          if (topicInfo.getTeamname().length() > 9)
            topicInfo.setTeamname(topicInfo.getTeamname().substring(0, 8) + "...");
        });
  }

  private List<TopicInfo> getTopicsPaginated(
      String env,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      Integer teamId,
      String topicType,
      int tenantId) {
    if (topicNameSearch != null) {
      topicNameSearch = topicNameSearch.trim();
    }

    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();

    // To get Producer or Consumer topics, first get all topics based on acls and then filter
    List<Topic> producerConsumerTopics = new ArrayList<>();
    if ((AclType.PRODUCER.value.equals(topicType) || AclType.CONSUMER.value.equals(topicType))
        && teamId != 0) {
      producerConsumerTopics =
          handleDbRequests.getAllTopicsByTopictypeAndTeamname(topicType, teamId, tenantId);

      // tenant filtering, not really necessary though, as based on team is searched.
      producerConsumerTopics =
          commonUtilsService.getFilteredTopicsForTenant(producerConsumerTopics);

      // select all topics and then filter
      env = "ALL";
      teamId = 1;
    }

    // Get Sync topics
    List<Topic> topicsFromSOT = commonUtilsService.getTopics(env, teamId, tenantId);
    topicsFromSOT = commonUtilsService.getFilteredTopicsForTenant(topicsFromSOT);

    // tenant filtering
    List<Env> listAllEnvs = manageDatabase.getKafkaEnvList(tenantId);
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);

    topicsFromSOT = commonUtilsService.groupTopicsByEnv(topicsFromSOT);
    List<Topic> filterProducerConsumerList = new ArrayList<>();

    topicsFromSOT =
        getProducerConsumerFilterTopics(
            producerConsumerTopics, topicsFromSOT, filterProducerConsumerList);

    List<Topic> topicFilteredList = topicsFromSOT;
    topicFilteredList =
        getTopicsFromTopicSearchFilters(topicNameSearch, topicsFromSOT, topicFilteredList);

    topicsFromSOT =
        topicFilteredList.stream().sorted(new TopicNameComparator()).collect(Collectors.toList());

    return getTopicInfoList(topicsFromSOT, pageNo, currentPage, listAllEnvs, orderOfEnvs, tenantId);
  }

  private static List<Topic> getTopicsFromTopicSearchFilters(
      String topicNameSearch, List<Topic> topicsFromSOT, List<Topic> topicFilteredList) {
    // Filter topics on topic name for search
    if (topicNameSearch != null && topicNameSearch.length() > 0) {
      final String topicSearchFilter = topicNameSearch;
      topicFilteredList =
          topicsFromSOT.stream()
              .filter(
                  topic ->
                      topic.getTopicname().toLowerCase().contains(topicSearchFilter.toLowerCase()))
              .collect(Collectors.toList());

      // searching documentation
      List<Topic> searchDocList =
          topicsFromSOT.stream()
              .filter(
                  topic ->
                      (topic.getDocumentation() != null
                          && topic
                              .getDocumentation()
                              .toLowerCase()
                              .contains(topicSearchFilter.toLowerCase())))
              .toList();

      topicFilteredList.addAll(searchDocList);
      topicFilteredList =
          new ArrayList<>(
              topicFilteredList.stream()
                  .collect(
                      Collectors.toConcurrentMap(
                          Topic::getTopicname, Function.identity(), (p, q) -> p))
                  .values());
    }
    return topicFilteredList;
  }

  private static List<Topic> getProducerConsumerFilterTopics(
      List<Topic> producerConsumerTopics,
      List<Topic> topicsFromSOT,
      List<Topic> filterProducerConsumerList) {
    String tmpTopicFull;
    String tmpTopicSub;
    if (!producerConsumerTopics.isEmpty()) {
      for (Topic topicInfo : topicsFromSOT) {
        for (Topic producerConsumerTopic : producerConsumerTopics) {
          tmpTopicFull = producerConsumerTopic.getTopicname();

          String prefixVar = "--";
          if (tmpTopicFull.endsWith(
              prefixVar + AclPatternType.PREFIXED + prefixVar)) { // has prefixed acl
            tmpTopicSub =
                tmpTopicFull.replaceAll(prefixVar + AclPatternType.PREFIXED + prefixVar, "");
            if (topicInfo.getTopicname().startsWith(tmpTopicSub)
                && topicInfo
                    .getEnvironmentsList()
                    .contains(producerConsumerTopic.getEnvironment())) {
              topicInfo.setEnvironmentsList(producerConsumerTopic.getEnvironmentsList());
              filterProducerConsumerList.add(topicInfo);
            }
          } else if (Objects.equals(producerConsumerTopic.getTopicname(), topicInfo.getTopicname())
              && topicInfo.getEnvironmentsList().contains(producerConsumerTopic.getEnvironment())) {
            topicInfo.setEnvironmentsList(producerConsumerTopic.getEnvironmentsList());
            filterProducerConsumerList.add(topicInfo);
          }
        }
      }
      topicsFromSOT = filterProducerConsumerList;
    }
    return topicsFromSOT;
  }

  private List<List<TopicInfo>> getPagedList(List<TopicInfo> topicsList) {

    List<List<TopicInfo>> newList = new ArrayList<>();
    List<TopicInfo> innerList = new ArrayList<>();
    int modulusFactor = 3;
    int i = 0;
    for (TopicInfo topicInfo : topicsList) {

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

  private int counterIncrement() {
    topicCounter++;
    return topicCounter;
  }

  private List<String> getConvertedEnvs(List<Env> allEnvs, List<String> selectedEnvs) {
    List<String> newEnvList = new ArrayList<>();
    for (String env : selectedEnvs) {
      for (Env env1 : allEnvs) {
        if (Objects.equals(env, env1.getId())) {
          newEnvList.add(env1.getName());
          break;
        }
      }
    }

    return newEnvList;
  }

  private List<TopicInfo> getTopicInfoList(
      List<Topic> topicsFromSOT,
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

    List<TopicInfo> topicsListMap = null;
    if (totalRecs > 0) {
      topicsListMap = new ArrayList<>();
    }

    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < topicsFromSOT.size(); i++) {
      int counterInc = counterIncrement();
      if (i >= startVar && i < lastVar) {
        TopicInfo mp = new TopicInfo();
        mp.setSequence(counterInc + "");
        Topic topicSOT = topicsFromSOT.get(i);

        List<String> envList = topicSOT.getEnvironmentsList();
        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        mp.setTopicid(topicSOT.getTopicid());
        mp.setEnvId(topicSOT.getEnvironment());
        mp.setEnvironmentsList(getConvertedEnvs(listAllEnvs, envList));
        mp.setTopicName(topicSOT.getTopicname());
        mp.setTeamId(topicSOT.getTeamId());
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topicSOT.getTeamId()));

        mp.setNoOfReplicas(topicSOT.getNoOfReplicas());
        mp.setNoOfPartitions(topicSOT.getNoOfPartitions());
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

  public String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  public Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public Env getEnvDetails(String envId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(commonUtilsService.getTenantId(getUserName())).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  public Map<String, String> getTopicEvents(
      String envId, String consumerGroupId, String topicName, String offsetId) {
    Map<String, String> topicEvents = new TreeMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    try {
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA, tenantId)
              .get(getEnvDetails(envId).getClusterId());
      topicEvents =
          clusterApiService.getTopicEvents(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              topicName,
              offsetId,
              consumerGroupId,
              tenantId);
    } catch (Exception e) {
      log.error("Ignoring error while retrieving topic events ", e);
      topicEvents.put("status", "false");
    }
    if (topicEvents != null && topicEvents.size() == 0) {
      topicEvents.put("status", "false");
    }

    return topicEvents;
  }

  public List<TopicRequest> getExistingTopicRequests(
      TopicRequestModel topicRequestModel, int tenantId) {
    return manageDatabase
        .getHandleDbRequests()
        .getTopicRequests(
            topicRequestModel.getTopicname(),
            topicRequestModel.getEnvironment(),
            RequestStatus.CREATED.value,
            tenantId);
  }

  public String getSyncCluster(int tenantId) {
    return manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
  }

  public List<Topic> getTopicFromName(String topicName, int tenantId) {
    List<Topic> topics = commonUtilsService.getTopicsForTopicName(topicName, tenantId);

    // tenant filtering
    topics = commonUtilsService.getFilteredTopicsForTenant(topics);
    return topics;
  }
}
