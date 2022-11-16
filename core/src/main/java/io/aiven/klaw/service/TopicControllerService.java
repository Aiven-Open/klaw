package io.aiven.klaw.service;

import static io.aiven.klaw.model.MailType.TOPIC_CLAIM_REQUESTED;
import static io.aiven.klaw.model.MailType.TOPIC_CREATE_REQUESTED;
import static io.aiven.klaw.model.MailType.TOPIC_DELETE_REQUESTED;
import static io.aiven.klaw.model.MailType.TOPIC_REQUEST_APPROVED;
import static io.aiven.klaw.model.MailType.TOPIC_REQUEST_DENIED;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclPatternType;
import io.aiven.klaw.model.AclType;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.KafkaClustersType;
import io.aiven.klaw.model.PermissionType;
import io.aiven.klaw.model.RequestStatus;
import io.aiven.klaw.model.TopicConfigEntry;
import io.aiven.klaw.model.TopicConfiguration;
import io.aiven.klaw.model.TopicConfigurationRequest;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.TopicRequestTypes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TopicControllerService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

  public ApiResponse createTopicsRequest(TopicRequestModel topicRequestReq) throws KlawException {
    log.info("createTopicsRequest {}", topicRequestReq);
    String userName = getUserName();

    //    if (commonUtilsService.isNotAuthorizedUser(
    //        getPrincipal(), PermissionType.REQUEST_CREATE_TOPICS)) {
    //      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    //    }

    topicRequestReq.setRequestor(userName);
    topicRequestReq.setUsername(userName);
    topicRequestReq.setTeamId(getMyTeamId(userName));

    //    int topicPartitions = topicRequestReq.getTopicpartitions();
    //    String topicRf = topicRequestReq.getReplicationfactor();
    //    String envSelected = topicRequestReq.getEnvironment();

    // tenant filtering
    //    if (!getEnvsFromUserId(userName).contains(envSelected)) {
    //      return ApiResponse.builder()
    //          .result("Failure. Not authorized to request topic for this environment.")
    //          .build();
    //    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    // tenant filtering
    //    int tenantId = commonUtilsService.getTenantId(getUserName());
    //    String syncCluster =
    // manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    //    try {
    //      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    //    } catch (Exception e) {
    //      log.error("Tenant Configuration not found. " + tenantId, e);
    //      return ApiResponse.builder()
    //          .result("Failure. Tenant configuration in Server config is missing. Please
    // configure.")
    //          .build();
    //    }
    //    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");

    //    if (topicRequestReq.getTopicname() == null || topicRequestReq.getTopicname().length() ==
    // 0) {
    //      return ApiResponse.builder().result("Failure. Please fill in topic name.").build();
    //    }
    //    List<Topic> topics = getTopicFromName(topicRequestReq.getTopicname(), tenantId);

    //    if (topics != null
    //        && topics.size() > 0
    //        && !Objects.equals(topics.get(0).getTeamId(), topicRequestReq.getTeamId())) {
    //      return ApiResponse.builder()
    //          .result("Failure. This topic is owned by a different team.")
    //          .build();
    //    }
    //    boolean promotionOrderCheck =
    //        checkInPromotionOrder(
    //            topicRequestReq.getTopicname(), topicRequestReq.getEnvironment(), orderOfEnvs);
    //
    //    if (topics != null && topics.size() > 0) {
    //      if (promotionOrderCheck) {
    //        int devTopicFound =
    //            (int)
    //                topics.stream()
    //                    .filter(topic -> Objects.equals(topic.getEnvironment(), syncCluster))
    //                    .count();
    //        if (devTopicFound != 1) {
    //          if (getEnvDetails(syncCluster) == null) {
    //            return ApiResponse.builder()
    //                .result("Failure. This topic does not exist in base cluster.")
    //                .build();
    //          } else {
    //            return ApiResponse.builder()
    //                .result(
    //                    "Failure. This topic does not exist in "
    //                        + getEnvDetails(syncCluster).getName()
    //                        + " cluster.")
    //                .build();
    //          }
    //        }
    //      }
    //    } else if (!Objects.equals(topicRequestReq.getEnvironment(), syncCluster)) {
    //      if (promotionOrderCheck) {
    //        return ApiResponse.builder()
    //            .result(
    //                "Failure. Please request for a topic first in "
    //                    + getEnvDetails(syncCluster).getName()
    //                    + " cluster.")
    //            .build();
    //      }
    //    }

    //    if (topics != null) {
    //      if (manageDatabase
    //              .getHandleDbRequests()
    //              .selectTopicRequests(
    //                  topicRequestReq.getTopicname(),
    //                  topicRequestReq.getEnvironment(),
    //                  RequestStatus.created.name(),
    //                  tenantId)
    //              .size()
    //          > 0) {
    //        return ApiResponse.builder().result("Failure. A topic request already
    // exists.").build();
    //      }
    //    }

    // Ignore topic exists check if Update request
    //    if (!TopicRequestTypes.Update.name().equals(topicRequestReq.getTopictype())) {
    //      boolean topicExists = false;
    //      if (topics != null) {
    //        topicExists =
    //            topics.stream()
    //                .anyMatch(
    //                    topicEx ->
    //                        Objects.equals(topicEx.getEnvironment(),
    // topicRequestReq.getEnvironment()));
    //      }
    //      if (topicExists) {
    //        return ApiResponse.builder()
    //            .result("Failure. This topic already exists in the selected cluster.")
    //            .build();
    //      }
    //    }

    //    Env env = getEnvDetails(topicRequestReq.getEnvironment());
    //    Map<String, String> isValidTopicMap =
    //        validateParameters(topicRequestReq, env);
    //    String validTopicStatus = isValidTopicMap.get("status");

    //    if ("true".equals(validTopicStatus)) {
    TopicRequest topicRequestDao = new TopicRequest();
    copyProperties(topicRequestReq, topicRequestDao);
    mapAdvancedTopicConfiguration(topicRequestReq, topicRequestDao);
    topicRequestDao.setTenantId(commonUtilsService.getTenantId(getUserName()));

    mailService.sendMail(
        topicRequestReq.getTopicname(),
        null,
        "",
        userName,
        dbHandle,
        TOPIC_CREATE_REQUESTED,
        commonUtilsService.getLoginUrl());
    return ApiResponse.builder()
        .result(dbHandle.requestForTopic(topicRequestDao).get("result"))
        .build();
    //    } else {
    //      return ApiResponse.builder().result(isValidTopicMap.get("error")).build();
    //    }
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
      throw new KlawException("Error in processing advanced topic configs");
    }
  }

  //  private boolean checkInPromotionOrder(String topicname, String envId, String orderOfEnvs) {
  //    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
  //    return orderedEnv.contains(envId);
  //  }

  //  private Map<String, String> validateParameters(
  //      TopicRequestModel topicRequestReq, Env env) {
  //    log.debug("Into validateParameters");
  //
  //    Map<String, String> validMap = new HashMap<>();
  //    validMap.put("status", "true");
  //
  //    String topicPrefix = null, topicSuffix = null;
  //    String otherParams = env.getOtherParams();
  //    String[] params;
  //    try {
  //      if (otherParams != null) {
  //        params = otherParams.split(",");
  //        for (String param : params) {
  //          if (param.startsWith("topic.prefix")) {
  //            topicPrefix = param.substring(param.indexOf("=") + 1);
  //          } else if (param.startsWith("topic.suffix")) {
  //            topicSuffix = param.substring(param.indexOf("=") + 1);
  //          }
  //        }
  //      }
  //    } catch (Exception e) {
  //      log.error("Unable to set topic partitions, setting default from properties.", e);
  //    }
  //
  //    try {
  //      if (topicPrefix != null
  //          && topicPrefix.length() > 0
  //          && !topicRequestReq.getTopicname().startsWith(topicPrefix)) {
  //        log.error(
  //            "Topic prefix {} does not match. {}", topicPrefix, topicRequestReq.getTopicname());
  //        validMap.put("status", "false");
  //        validMap.put("error", "Topic prefix does not match. " + topicRequestReq.getTopicname());
  //      }
  //      if (topicSuffix != null
  //          && topicSuffix.length() > 0
  //          && !topicRequestReq.getTopicname().endsWith(topicSuffix)) {
  //        log.error(
  //            "Topic suffix {} does not match. {}", topicSuffix, topicRequestReq.getTopicname());
  //        validMap.put("status", "false");
  //        validMap.put("error", "Topic suffix does not match. " + topicRequestReq.getTopicname());
  //      }
  //    } catch (Exception e) {
  //      log.error("Unable to set topic partitions, setting default from properties.", e);
  //      validMap.put("status", "false");
  //      validMap.put("error", "Cluster default parameters config missing/incorrect.");
  //    }
  //    return validMap;
  //  }

  // create a request to delete topic.
  public ApiResponse createTopicDeleteRequest(String topicName, String envId) throws KlawException {
    log.info("createTopicDeleteRequest {} {}", topicName, envId);
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_TOPICS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    TopicRequest topicRequestReq = new TopicRequest();

    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<Topic> topics = getTopicFromName(topicName, tenantId);

    Integer userTeamId = getMyTeamId(userDetails);
    if (topics != null
        && topics.size() > 0
        && !Objects.equals(topics.get(0).getTeamId(), userTeamId)) {
      return ApiResponse.builder()
          .result("Failure. Sorry, you cannot delete this topic, as you are not part of this team.")
          .build();
    }

    topicRequestReq.setRequestor(userDetails);
    topicRequestReq.setUsername(userDetails);
    topicRequestReq.setTeamId(userTeamId);
    topicRequestReq.setEnvironment(envId);
    topicRequestReq.setTopicname(topicName);
    topicRequestReq.setTopictype(TopicRequestTypes.Delete.name());
    topicRequestReq.setTenantId(tenantId);

    Optional<Topic> topicOb =
        getTopicFromName(topicName, tenantId).stream()
            .filter(
                topic -> Objects.equals(topic.getEnvironment(), topicRequestReq.getEnvironment()))
            .findFirst();

    if (manageDatabase
            .getHandleDbRequests()
            .selectTopicRequests(
                topicRequestReq.getTopicname(),
                topicRequestReq.getEnvironment(),
                RequestStatus.created.name(),
                tenantId)
            .size()
        > 0) {
      return ApiResponse.builder()
          .result("Failure. A delete topic request already exists.")
          .build();
    }

    if (topicOb.isPresent()) {
      // Check if any existing subscriptions for this topic
      List<Acl> acls =
          manageDatabase
              .getHandleDbRequests()
              .getSyncAcls(
                  topicRequestReq.getEnvironment(), topicRequestReq.getTopicname(), tenantId);
      if (acls.size() > 0) {
        return ApiResponse.builder()
            .result(
                "Failure. There are existing subscriptions for topic. Please get them deleted before.")
            .build();
      }

      topicRequestReq.setTopicpartitions(topicOb.get().getNoOfPartitions());
      topicRequestReq.setReplicationfactor(topicOb.get().getNoOfReplcias());

      try {
        mailService.sendMail(
            topicRequestReq.getTopicname(),
            null,
            "",
            userDetails,
            dbHandle,
            TOPIC_DELETE_REQUESTED,
            commonUtilsService.getLoginUrl());

        String result =
            manageDatabase.getHandleDbRequests().requestForTopic(topicRequestReq).get("result");
        return ApiResponse.builder().result(result).build();
      } catch (Exception e) {
        log.error("Error ", e);
        throw new KlawException(e.getMessage());
      }
    } else {
      log.error("Topic not found : {}", topicName);
      return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
    }
  }

  public ApiResponse createClaimTopicRequest(String topicName, String env) throws KlawException {
    log.info("createClaimTopicRequest {}", topicName);
    String userDetails = getUserName();

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    TopicRequest topicRequestReq = new TopicRequest();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (manageDatabase
            .getHandleDbRequests()
            .selectTopicRequests(
                topicName,
                getEnvDetailsFromName(env, tenantId).getId(),
                RequestStatus.created.name(),
                tenantId)
            .size()
        > 0) {
      return ApiResponse.builder()
          .result("Failure. A request already exists for this topic.")
          .build();
    }

    List<Topic> topics = getTopicFromName(topicName, tenantId);
    Integer topicOwnerTeamId = topics.get(0).getTeamId();
    Optional<UserInfo> topicOwnerContact =
        manageDatabase.getHandleDbRequests().selectAllUsersInfo(tenantId).stream()
            .filter(user -> Objects.equals(user.getTeamId(), topicOwnerTeamId))
            .findFirst();

    Integer userTeamId = getMyTeamId(userDetails);

    topicRequestReq.setRequestor(userDetails);
    topicRequestReq.setUsername(userDetails);
    topicRequestReq.setTeamId(userTeamId);
    topicRequestReq.setTenantId(tenantId);
    topicRequestReq.setEnvironment(getEnvDetailsFromName(env, tenantId).getId());
    topicRequestReq.setTopicname(topicName);
    topicRequestReq.setTopictype(TopicRequestTypes.Claim.name());
    topicRequestReq.setDescription(topicOwnerTeamId + "");
    topicRequestReq.setRemarks("Topic Claim request for all available environments.");

    mailService.sendMail(
        topicRequestReq.getTopicname(),
        null,
        "",
        userDetails,
        dbHandle,
        TOPIC_CLAIM_REQUESTED,
        commonUtilsService.getLoginUrl());

    topicOwnerContact.ifPresent(
        userInfo ->
            mailService.sendMail(
                topicRequestReq.getTopicname(),
                null,
                "",
                userInfo.getUsername(),
                dbHandle,
                TOPIC_CLAIM_REQUESTED,
                commonUtilsService.getLoginUrl()));

    try {
      String result =
          manageDatabase.getHandleDbRequests().requestForTopic(topicRequestReq).get("result");
      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Error ", e);
      throw new KlawException(e.getMessage());
    }
  }

  public List<TopicRequestModel> getTopicRequests(
      String pageNo, String currentPage, String requestsType) {
    log.debug("getTopicRequests page {} requestsType {}", pageNo, requestsType);
    String userDetails = getUserName();
    List<TopicRequest> topicReqs =
        manageDatabase
            .getHandleDbRequests()
            .getAllTopicRequests(userDetails, commonUtilsService.getTenantId(getUserName()));

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(userDetails);
    topicReqs =
        topicReqs.stream()
            .filter(topicRequest -> allowedEnvIdList.contains(topicRequest.getEnvironment()))
            .sorted(Collections.reverseOrder(Comparator.comparing(TopicRequest::getRequesttime)))
            .collect(Collectors.toList());

    if (!"all".equals(requestsType) && EnumUtils.isValidEnum(RequestStatus.class, requestsType)) {
      topicReqs =
          topicReqs.stream()
              .filter(topicRequest -> Objects.equals(topicRequest.getTopicstatus(), requestsType))
              .collect(Collectors.toList());
    }

    topicReqs = getTopicRequestsPaged(topicReqs, pageNo, currentPage);
    return getTopicRequestModels(topicReqs, true);
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

  public List<Topic> getTopicFromName(String topicName, int tenantId) {
    List<Topic> topics = manageDatabase.getHandleDbRequests().getTopicTeam(topicName, tenantId);

    // tenant filtering
    topics = getFilteredTopicsForTenant(topics);

    return topics;
  }

  public Map<String, String> getTopicTeamOnly(String topicName, String patternType) {
    log.debug("getTopicTeamOnly {} {}", topicName, patternType);
    Map<String, String> teamMap = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (AclPatternType.PREFIXED.value.equals(patternType)) {
      List<Topic> topics = manageDatabase.getHandleDbRequests().getAllTopics(tenantId);

      // tenant filtering
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      topics =
          topics.stream()
              .filter(topicRequest -> allowedEnvIdList.contains(topicRequest.getEnvironment()))
              .distinct()
              .collect(Collectors.toList());

      List<Topic> alltopicsStartingWithPattern = new ArrayList<>();

      topics.forEach(
          topic -> {
            if (topic.getTopicname().startsWith(topicName)) {
              alltopicsStartingWithPattern.add(topic);
            }
          });

      if (alltopicsStartingWithPattern.size() == 0) {
        teamMap.put(
            "error", "There are no topics found with this prefix. You may synchronize metadata.");
        return teamMap;
      }

      List<Integer> stringTeamsFound = new ArrayList<>();
      alltopicsStartingWithPattern.forEach(top -> stringTeamsFound.add(top.getTeamId()));

      List<Integer> updatedTeamList =
          stringTeamsFound.stream().distinct().collect(Collectors.toList());
      if (updatedTeamList.size() > 1) {
        teamMap.put(
            "error", "There are atleast two topics with same prefix owned by different teams.");
      } else {
        teamMap.put("team", manageDatabase.getTeamNameFromTeamId(tenantId, updatedTeamList.get(0)));
      }
      return teamMap;
    } else {
      List<Topic> topics = manageDatabase.getHandleDbRequests().getTopicTeam(topicName, tenantId);
      // tenant filtering
      topics = getFilteredTopicsForTenant(topics);

      if (topics.size() > 0) {
        teamMap.put(
            "team", manageDatabase.getTeamNameFromTeamId(tenantId, topics.get(0).getTeamId()));
      } else {
        teamMap.put("error", "No team found");
      }
      return teamMap;
    }
  }

  public List<TopicRequestModel> getCreatedTopicRequests(
      String pageNo, String currentPage, String requestsType) {
    log.debug("getCreatedTopicRequests {} {}", pageNo, requestsType);
    String userDetails = getUserName();
    List<TopicRequest> createdTopicReqList;
    int tenantId = commonUtilsService.getTenantId(getUserName());
    // get requests relevant to your teams or all teams
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS)) {
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedTopicRequests(userDetails, requestsType, false, tenantId);
    } else {
      createdTopicReqList =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedTopicRequests(userDetails, requestsType, true, tenantId);
    }

    createdTopicReqList = getTopicRequestsFilteredForTenant(createdTopicReqList);
    createdTopicReqList = getTopicRequestsPaged(createdTopicReqList, pageNo, currentPage);

    return updateCreateTopicReqsList(createdTopicReqList, tenantId);
  }

  private List<TopicRequestModel> updateCreateTopicReqsList(
      List<TopicRequest> topicsList, int tenantId) {
    List<TopicRequestModel> topicRequestModelList = getTopicRequestModels(topicsList, true);

    for (TopicRequestModel topicInfo : topicRequestModelList) {
      topicInfo.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topicInfo.getTeamId()));
      topicInfo.setEnvironmentName(getEnvDetails(topicInfo.getEnvironment()).getName());
    }

    return topicRequestModelList;
  }

  private List<TopicRequestModel> getTopicRequestModels(
      List<TopicRequest> topicsList, boolean fromSyncTopics) {
    List<TopicRequestModel> topicRequestModelList = new ArrayList<>();
    TopicRequestModel topicRequestModel;
    Integer userTeamId = getMyTeamId(getUserName());

    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("TOPICS", tenantId);
    List<UserInfo> userList =
        manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(userTeamId, tenantId);

    for (TopicRequest topicReq : topicsList) {
      topicRequestModel = new TopicRequestModel();
      copyProperties(topicReq, topicRequestModel);
      validateAndCopyTopicConfigs(topicReq, topicRequestModel);

      topicRequestModel.setTeamname(
          manageDatabase.getTeamNameFromTeamId(tenantId, topicReq.getTeamId()));

      if (fromSyncTopics) {
        // show approving info only before approvals
        if (!RequestStatus.approved.name().equals(topicRequestModel.getTopicstatus())) {
          if (topicRequestModel.getTopictype() != null
              && TopicRequestTypes.Claim.name().equals(topicRequestModel.getTopictype())) {
            List<Topic> topics = getTopicFromName(topicRequestModel.getTopicname(), tenantId);
            topicRequestModel.setApprovingTeamDetails(
                updateApproverInfo(
                    manageDatabase
                        .getHandleDbRequests()
                        .selectAllUsersInfoForTeam(topics.get(0).getTeamId(), tenantId),
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
      }

      topicRequestModelList.add(topicRequestModel);
    }
    return topicRequestModelList;
  }

  private void validateAndCopyTopicConfigs(
      TopicRequest topicReq, TopicRequestModel topicRequestModel) {
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
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }
    try {
      String deleteTopicReqStatus =
          manageDatabase
              .getHandleDbRequests()
              .deleteTopicRequest(
                  Integer.parseInt(topicId), commonUtilsService.getTenantId(getUserName()));

      return ApiResponse.builder().result(deleteTopicReqStatus).build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse approveTopicRequests(String topicId) throws KlawException {
    log.info("approveTopicRequests {}", topicId);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_TOPICS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    TopicRequest topicRequest =
        manageDatabase
            .getHandleDbRequests()
            .selectTopicRequestsForTopic(Integer.parseInt(topicId), tenantId);

    if (Objects.equals(topicRequest.getRequestor(), userDetails)) {
      return ApiResponse.builder()
          .result("You are not allowed to approve your own topic requests.")
          .build();
    }

    if (!RequestStatus.created.name().equals(topicRequest.getTopicstatus())) {
      return ApiResponse.builder().result("This request does not exist anymore.").build();
    }

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    if (!allowedEnvIdList.contains(topicRequest.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    String updateTopicReqStatus;
    if (TopicRequestTypes.Claim.name().equals(topicRequest.getTopictype())) {
      List<Topic> allTopics = getTopicFromName(topicRequest.getTopicname(), tenantId);
      for (Topic allTopic : allTopics) {
        allTopic.setTeamId(topicRequest.getTeamId()); // for claim reqs, team stored in description
        allTopic.setExistingTopic(true);
      }

      updateTopicReqStatus = dbHandle.addToSynctopics(allTopics);
      if (ApiResultStatus.SUCCESS.value.equals(updateTopicReqStatus)) {
        updateTopicReqStatus = dbHandle.updateTopicRequestStatus(topicRequest, userDetails);
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
      ResponseEntity<ApiResponse> response =
          clusterApiService.approveTopicRequests(
              topicRequest.getTopicname(),
              topicRequest.getTopictype(),
              topicRequest.getTopicpartitions(),
              topicRequest.getReplicationfactor(),
              topicRequest.getEnvironment(),
              topicConfig,
              tenantId);

      updateTopicReqStatus = Objects.requireNonNull(response.getBody()).getResult();

      if (ApiResultStatus.SUCCESS.value.equals(response.getBody().getResult())) {
        setTopicHistory(topicRequest, userDetails, tenantId);
        updateTopicReqStatus = dbHandle.updateTopicRequest(topicRequest, userDetails);
        mailService.sendMail(
            topicRequest.getTopicname(),
            null,
            "",
            topicRequest.getRequestor(),
            dbHandle,
            TOPIC_REQUEST_APPROVED,
            commonUtilsService.getLoginUrl());
      }
    }

    return ApiResponse.builder().result(updateTopicReqStatus).build();
  }

  private void setTopicHistory(TopicRequest topicRequest, String userName, int tenantId) {
    try {
      AtomicReference<String> existingHistory = new AtomicReference<>("");
      List<TopicHistory> existingTopicHistory;
      List<TopicHistory> topicHistoryList = new ArrayList<>();

      if (TopicRequestTypes.Update.name().equals(topicRequest.getTopictype())) {
        List<Topic> existingTopicList =
            getTopicFromName(topicRequest.getTopicname(), topicRequest.getTenantId());
        existingTopicList.stream()
            .filter(topic -> Objects.equals(topic.getEnvironment(), topicRequest.getEnvironment()))
            .findFirst()
            .ifPresent(a -> existingHistory.set(a.getHistory()));
        existingTopicHistory = OBJECT_MAPPER.readValue(existingHistory.get(), ArrayList.class);
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
      topicHistory.setRemarks(topicRequest.getTopictype());
      topicHistoryList.add(topicHistory);

      topicRequest.setHistory(OBJECT_MAPPER.writer().writeValueAsString(topicHistoryList));
    } catch (Exception e) {
      log.error("Exception: ", e);
    }
  }

  public ApiResponse declineTopicRequests(String topicId, String reasonForDecline)
      throws KlawException {
    log.info("declineTopicRequests {} {}", topicId, reasonForDecline);
    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_TOPICS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    TopicRequest topicRequest =
        dbHandle.selectTopicRequestsForTopic(
            Integer.parseInt(topicId), commonUtilsService.getTenantId(getUserName()));

    if (!RequestStatus.created.name().equals(topicRequest.getTopicstatus())) {
      return ApiResponse.builder().result("This request does not exist anymore.").build();
    }

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    if (!allowedEnvIdList.contains(topicRequest.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    try {
      String result = dbHandle.declineTopicRequest(topicRequest, userDetails);
      mailService.sendMail(
          topicRequest.getTopicname(),
          null,
          reasonForDecline,
          topicRequest.getRequestor(),
          dbHandle,
          TOPIC_REQUEST_DENIED,
          commonUtilsService.getLoginUrl());

      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public List<String> getAllTopics(boolean isMyTeamTopics) {
    log.debug("getAllTopics {}", isMyTeamTopics);
    List<Topic> topicsFromSOT =
        manageDatabase
            .getHandleDbRequests()
            .getSyncTopics(null, null, commonUtilsService.getTenantId(getUserName()));

    // tenant filtering
    topicsFromSOT = getFilteredTopicsForTenant(topicsFromSOT);

    if (isMyTeamTopics) {
      Integer userTeamId = getMyTeamId(getUserName());
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
    topic.setTenantId(commonUtilsService.getTenantId(getUserName()));
    topic.setTopicid(topicInfo.getTopicid());
    topic.setDocumentation(topicInfo.getDocumentation());

    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<Topic> topicsSearchList =
        manageDatabase.getHandleDbRequests().getTopicTeam(topicInfo.getTopicName(), tenantId);

    try {
      // tenant filtering
      Integer topicOwnerTeamId = getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();
      Integer loggedInUserTeamId = getMyTeamId(getUserName());
      if (Objects.equals(topicOwnerTeamId, loggedInUserTeamId)) {
        return ApiResponse.builder()
            .result(manageDatabase.getHandleDbRequests().updateTopicDocumentation(topic))
            .build();
      } else {
        return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
      }
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public Map<String, Object> getTopicDetailsPerEnv(String envId, String topicName) {
    Map<String, Object> hashMap = new HashMap<>();
    hashMap.put("topicExists", false);
    hashMap.put("error", "Could not retrieve topic details.");

    TopicInfo topicInfo = new TopicInfo();
    List<Topic> topics =
        manageDatabase
            .getHandleDbRequests()
            .getTopics(topicName, commonUtilsService.getTenantId(getUserName()));

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    topics =
        topics.stream()
            .filter(topicObj -> allowedEnvIdList.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    int tenantId = commonUtilsService.getTenantId(getUserName());

    String topicDescription = "";
    if (topics.size() == 0) {
      hashMap.put("error", "Topic does not exist.");
      return hashMap;
    } else {
      Optional<Topic> topicDescFound =
          topics.stream()
              .filter(
                  topic -> topic.getDescription() != null && topic.getDescription().length() > 0)
              .findFirst();
      if (topicDescFound.isPresent()) {
        topicDescription = topicDescFound.get().getDescription();
      }
      topics =
          topics.stream()
              .filter(topic -> Objects.equals(topic.getEnvironment(), envId))
              .collect(Collectors.toList());
    }

    for (Topic topic : topics) {
      topicInfo.setCluster(getEnvDetails(topic.getEnvironment()).getName());
      topicInfo.setClusterId(topic.getEnvironment());
      topicInfo.setNoOfPartitions(topic.getNoOfPartitions());
      topicInfo.setNoOfReplcias(topic.getNoOfReplcias());
      topicInfo.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topic.getTeamId()));
      hashMap.put("topicId", "" + topic.getTopicid());
    }

    topicInfo.setDescription(topicDescription);

    String loggedInUserTeam =
        manageDatabase
            .getHandleDbRequests()
            .selectAllTeamsOfUsers(getUserName(), tenantId)
            .get(0)
            .getTeamname();
    if (!Objects.equals(loggedInUserTeam, topicInfo.getTeamname())) {
      hashMap.put("error", "Sorry, your team does not own the topic !!");
      return hashMap;
    }

    if (topicInfo.getNoOfPartitions() != null) {
      hashMap.put("topicExists", true);
      hashMap.put("topicContents", topicInfo);
    }
    return hashMap;
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

  static class TopicNameSyncComparator implements Comparator<Map<String, String>> {
    @Override
    public int compare(Map<String, String> topic1, Map<String, String> topic2) {
      return topic1.get("topicName").compareTo(topic2.get("topicName"));
    }
  }

  public List<List<TopicInfo>> getTopics(
      String env,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      String teamName,
      String topicType) {
    log.debug("getTopics {}", topicNameSearch);
    List<TopicInfo> topicListUpdated =
        getTopicsPaginated(
            env,
            pageNo,
            currentPage,
            topicNameSearch,
            teamName,
            topicType,
            commonUtilsService.getTenantId(getUserName()));

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
      String teamName,
      String topicType,
      int tenantId) {
    if (topicNameSearch != null) topicNameSearch = topicNameSearch.trim();

    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();

    // To get Producer or Consumer topics, first get all topics based on acls and then filter
    List<Topic> producerConsumerTopics = new ArrayList<>();
    if ((AclType.PRODUCER.value.equals(topicType) || AclType.CONSUMER.value.equals(topicType))
        && teamName != null) {
      producerConsumerTopics =
          handleDbRequests.selectAllTopicsByTopictypeAndTeamname(
              topicType, manageDatabase.getTeamIdFromTeamName(tenantId, teamName), tenantId);

      // tenant filtering, not really necessary though, as based on team is searched.
      producerConsumerTopics = getFilteredTopicsForTenant(producerConsumerTopics);

      // select all topics and then filter
      env = "ALL";
      teamName = null;
    }

    // Get Sync topics
    List<Topic> topicsFromSOT =
        handleDbRequests.getSyncTopics(
            env, manageDatabase.getTeamIdFromTeamName(tenantId, teamName), tenantId);
    topicsFromSOT = getFilteredTopicsForTenant(topicsFromSOT);

    // tenant filtering
    List<Env> listAllEnvs = manageDatabase.getKafkaEnvList(tenantId);
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");

    topicsFromSOT = groupTopicsByEnv(topicsFromSOT);
    List<Topic> filterProducerConsumerList = new ArrayList<>();
    String tmpTopicFull, tmpTopicSub;

    if (producerConsumerTopics.size() > 0) {
      for (Topic topicInfo : topicsFromSOT) {
        for (Topic producerConsumerTopic : producerConsumerTopics) {
          tmpTopicFull = producerConsumerTopic.getTopicname();

          if (tmpTopicFull.endsWith("--PREFIXED--")) { // has prefixed acl
            tmpTopicSub = tmpTopicFull.replaceAll("--PREFIXED--", "");
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

    List<Topic> topicFilteredList = topicsFromSOT;
    // Filter topics on topic name for search
    if (topicNameSearch != null && topicNameSearch.length() > 0) {
      final String topicSearchFilter = topicNameSearch;
      topicFilteredList =
          topicsFromSOT.stream()
              .filter(topic -> topic.getTopicname().contains(topicSearchFilter))
              .collect(Collectors.toList());

      // searching documentation
      List<Topic> searchDocList =
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
                          Topic::getTopicname, Function.identity(), (p, q) -> p))
                  .values());
    }

    topicsFromSOT =
        topicFilteredList.stream().sorted(new TopicNameComparator()).collect(Collectors.toList());

    return getTopicInfoList(topicsFromSOT, pageNo, currentPage, listAllEnvs, orderOfEnvs, tenantId);
  }

  private List<Topic> groupTopicsByEnv(List<Topic> topicsFromSOT) {
    List<Topic> tmpTopicList = new ArrayList<>();

    Map<String, List<Topic>> groupedList =
        topicsFromSOT.stream().collect(Collectors.groupingBy(Topic::getTopicname));
    groupedList.forEach(
        (k, v) -> {
          Topic t = v.get(0);
          List<String> tmpEnvList = new ArrayList<>();
          for (Topic topic : v) {
            tmpEnvList.add(topic.getEnvironment());
          }
          t.setEnvironmentsList(tmpEnvList);
          tmpTopicList.add(t);
        });
    return tmpTopicList;
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
        mp.setCluster(topicSOT.getEnvironment());
        mp.setEnvironmentsList(getConvertedEnvs(listAllEnvs, envList));
        mp.setTopicName(topicSOT.getTopicname());
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topicSOT.getTeamId()));

        mp.setNoOfReplcias(topicSOT.getNoOfReplcias());
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
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
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

  public Env getEnvDetailsFromName(String envName, Integer tenantId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(tenantId).stream()
            .filter(
                env ->
                    Objects.equals(env.getName(), envName)
                        && Objects.equals(env.getTenantId(), tenantId))
            .findFirst();
    return envFound.orElse(null);
  }

  public List<String> getEnvsFromUserId(String userDetails) {
    Integer userTeamId = getMyTeamId(userDetails);
    return manageDatabase.getTeamsAndAllowedEnvs(
        userTeamId, commonUtilsService.getTenantId(userDetails));
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }

  private List<Topic> getFilteredTopicsForTenant(List<Topic> topicsFromSOT) {
    // tenant filtering
    try {
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      if (topicsFromSOT != null) {
        topicsFromSOT =
            topicsFromSOT.stream()
                .filter(topic -> allowedEnvIdList.contains(topic.getEnvironment()))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return topicsFromSOT;
  }

  private List<TopicRequest> getTopicRequestsFilteredForTenant(
      List<TopicRequest> createdTopicReqList) {
    // tenant filtering
    try {
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      if (createdTopicReqList != null) {
        createdTopicReqList =
            createdTopicReqList.stream()
                .filter(topicRequest -> allowedEnvIdList.contains(topicRequest.getEnvironment()))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return createdTopicReqList;
  }

  public Map<String, String> getTopicEvents(
      String envId, String consumerGroupId, String topicName, String offsetId)
      throws KlawException {
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
        .selectTopicRequests(
            topicRequestModel.getTopicname(),
            topicRequestModel.getEnvironment(),
            RequestStatus.created.name(),
            tenantId);
  }
}
