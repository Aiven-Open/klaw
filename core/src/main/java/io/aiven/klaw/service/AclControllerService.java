package io.aiven.klaw.service;

import static io.aiven.klaw.model.enums.MailType.ACL_DELETE_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUEST_APPROVED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUEST_DENIED;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.AclRequestsModel;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicOverview;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AclControllerService {
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
  @Autowired ManageDatabase manageDatabase;

  @Autowired private final MailUtils mailService;

  @Autowired private final ClusterApiService clusterApiService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  @Autowired private CommonUtilsService commonUtilsService;

  AclControllerService(ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public ApiResponse createAcl(AclRequestsModel aclReq) throws KlawException {
    log.info("createAcl {}", aclReq);
    String userDetails = getUserName();
    aclReq.setAclType(RequestOperationType.CREATE.value);
    aclReq.setUsername(userDetails);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    aclReq.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, aclReq.getTeamname()));
    aclReq.setRequestingteam(getMyTeamId(userDetails));

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    List<Topic> topics =
        manageDatabase.getHandleDbRequests().getTopics(aclReq.getTopicname(), tenantId);
    boolean topicFound = false;
    String result;

    if (AclPatternType.LITERAL.value.equals(aclReq.getAclPatternType())) {
      for (Topic topic : topics) {
        if (Objects.equals(topic.getEnvironment(), aclReq.getEnvironment())) {
          topicFound = true;
          break;
        }
      }
      result = "Failure : Topic not found on target environment.";
      if (!topicFound) {
        return ApiResponse.builder().result(result).build();
      }
    }

    if (AclType.CONSUMER.value.equals(aclReq.getTopictype())) {
      if (AclPatternType.PREFIXED.value.equals(aclReq.getAclPatternType())) {
        result = "Failure : Please change the pattern to LITERAL for topic type.";
        return ApiResponse.builder().result(result).build();
      }
      if (validateTeamConsumerGroup(
          aclReq.getRequestingteam(), aclReq.getConsumergroup(), tenantId)) {
        result = "Failure : Consumer group " + aclReq.getConsumergroup() + " used by another team.";
        return ApiResponse.builder().result(result).build();
      }
    }

    String txnId;
    if (aclReq.getTransactionalId() != null) {
      txnId = aclReq.getTransactionalId().trim();
      if (txnId.length() > 0) aclReq.setTransactionalId(txnId);
    }

    AclRequests aclRequestsDao = new AclRequests();
    copyProperties(aclReq, aclRequestsDao);
    StringBuilder aclStr = new StringBuilder();
    String separatorAcl = "<ACL>";
    if (aclReq.getAcl_ip() != null) {
      for (int i = 0; i < aclReq.getAcl_ip().size(); i++) {
        if (i == 0) {
          aclStr.append(aclReq.getAcl_ip().get(i));
        } else {
          aclStr = new StringBuilder(aclStr + separatorAcl + aclReq.getAcl_ip().get(i));
        }
      }
      aclRequestsDao.setAcl_ip(aclStr.toString());
    }

    if (aclReq.getAcl_ssl() != null) {
      for (int i = 0; i < aclReq.getAcl_ssl().size(); i++) {
        if (i == 0) {
          aclStr.append(aclReq.getAcl_ssl().get(i));
        } else {
          aclStr = new StringBuilder(aclStr + separatorAcl + aclReq.getAcl_ssl().get(i));
        }
      }
      aclRequestsDao.setAcl_ssl(aclStr.toString());
    }

    if (aclReq.getAcl_ssl() == null || aclReq.getAcl_ssl().equals("null")) {
      aclRequestsDao.setAcl_ssl("User:*");
    }

    aclRequestsDao.setTenantId(tenantId);
    try {
      String execRes =
          manageDatabase.getHandleDbRequests().requestForAcl(aclRequestsDao).get("result");

      if (ApiResultStatus.SUCCESS.value.equals(execRes)) {
        mailService.sendMail(
            aclReq.getTopicname(),
            aclReq.getTopictype(),
            "",
            userDetails,
            manageDatabase.getHandleDbRequests(),
            ACL_REQUESTED,
            commonUtilsService.getLoginUrl());
      }
      return ApiResponse.builder().result(execRes).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public List<AclRequestsModel> getAclRequests(
      String pageNo, String currentPage, String requestsType) {

    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    List<AclRequests> aclReqs =
        dbHandle.getAllAclRequests(false, userDetails, "", requestsType, false, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(userDetails);
    aclReqs =
        aclReqs.stream()
            .filter(aclRequest -> allowedEnvIdList.contains(aclRequest.getEnvironment()))
            .collect(Collectors.toList());

    aclReqs =
        aclReqs.stream()
            .sorted(Collections.reverseOrder(Comparator.comparing(AclRequests::getRequesttime)))
            .collect(Collectors.toList());

    aclReqs = getAclRequestsPaged(aclReqs, pageNo, currentPage, tenantId);

    return getAclRequestsModels(aclReqs, tenantId);
  }

  private List<AclRequestsModel> getAclRequestsModels(List<AclRequests> aclReqs, int tenantId) {
    List<AclRequestsModel> aclRequestsModels = new ArrayList<>();
    AclRequestsModel aclRequestsModel;

    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("SUBSCRIPTIONS", tenantId);

    if (aclReqs != null)
      for (AclRequests aclRequests : aclReqs) {
        aclRequestsModel = new AclRequestsModel();
        copyProperties(aclRequests, aclRequestsModel);
        if (aclRequests.getAcl_ip() != null) {
          String[] aclListIp = aclRequests.getAcl_ip().split("<ACL>");
          aclRequestsModel.setAcl_ip(new ArrayList<>(Arrays.asList(aclListIp)));
        }

        if (aclRequests.getAcl_ssl() != null) {
          String[] aclListSsl = aclRequests.getAcl_ssl().split("<ACL>");
          aclRequestsModel.setAcl_ssl(new ArrayList<>(Arrays.asList(aclListSsl)));
        }
        aclRequestsModel.setTeamname(
            manageDatabase.getTeamNameFromTeamId(tenantId, aclRequests.getTeamId()));

        // show approving info only before approvals
        if (!RequestStatus.approved.name().equals(aclRequestsModel.getAclstatus())) {
          aclRequestsModel.setApprovingTeamDetails(
              updateApprovingInfo(
                  aclRequestsModel.getTopicname(),
                  aclRequestsModel.getAclType(),
                  aclRequestsModel.getRequestingteam(),
                  approverRoles,
                  aclRequestsModel.getUsername(),
                  tenantId));
        }

        aclRequestsModels.add(aclRequestsModel);
      }
    return aclRequestsModels;
  }

  private String updateApprovingInfo(
      String topicName,
      String aclType,
      Integer team,
      List<String> approverRoles,
      String requestor,
      int tenantId) {
    List<Topic> topicTeamsList =
        manageDatabase.getHandleDbRequests().getTopicTeam(topicName, tenantId);
    if (topicTeamsList.size() > 0) {
      Integer teamId = getFilteredTopicsForTenant(topicTeamsList).get(0).getTeamId();

      if (RequestOperationType.DELETE.value.equals(aclType)) teamId = team;
      List<UserInfo> userList =
          manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(teamId, tenantId);

      StringBuilder approvingInfo =
          new StringBuilder(
              "Team : " + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ", Users : ");

      for (UserInfo userInfo : userList) {
        if (approverRoles.contains(userInfo.getRole())
            && !Objects.equals(requestor, userInfo.getUsername())) {
          approvingInfo.append(userInfo.getUsername()).append(",");
        }
      }
      return String.valueOf(approvingInfo);
    }

    return "";
  }

  public List<AclRequestsModel> getAclRequestModelPaged(
      List<AclRequestsModel> origActivityList, String pageNo, String currentPage, int tenantId) {
    List<AclRequestsModel> newList = new ArrayList<>();

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
        AclRequestsModel aclRequestsModel = origActivityList.get(i);
        if (i >= startVar && i < lastVar) {
          aclRequestsModel.setAllPageNos(numList);
          aclRequestsModel.setTotalNoPages("" + totalPages);
          aclRequestsModel.setCurrentPage(pageNo);
          aclRequestsModel.setTeamname(
              manageDatabase.getTeamNameFromTeamId(tenantId, aclRequestsModel.getTeamId()));
          aclRequestsModel.setEnvironmentName(
              getEnvDetails(aclRequestsModel.getEnvironment(), tenantId).getName());

          newList.add(aclRequestsModel);
        }
      }
    }

    return newList;
  }

  public List<AclRequests> getAclRequestsPaged(
      List<AclRequests> origActivityList, String pageNo, String currentPage, int tenantId) {
    List<AclRequests> newList = new ArrayList<>();

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
        AclRequests activityLog = origActivityList.get(i);
        if (i >= startVar && i < lastVar) {
          activityLog.setAllPageNos(numList);
          activityLog.setTotalNoPages("" + totalPages);
          activityLog.setCurrentPage(pageNo);
          activityLog.setEnvironmentName(
              getEnvDetails(activityLog.getEnvironment(), tenantId).getName());

          newList.add(activityLog);
        }
      }
    }

    return newList;
  }

  public List<AclRequestsModel> getCreatedAclRequests(
      String pageNo, String currentPage, String requestsType) {
    log.debug("getCreatedAclRequests {} {}", pageNo, requestsType);
    String userDetails = getUserName();
    List<AclRequests> createdAclReqs;
    int tenantId = commonUtilsService.getTenantId(userDetails);

    // get requests relevant to your teams or all teams
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS)) {
      createdAclReqs =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedAclRequestsByStatus(userDetails, requestsType, false, tenantId);
    } else {
      createdAclReqs =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedAclRequestsByStatus(userDetails, requestsType, true, tenantId);
    }

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(userDetails);
    createdAclReqs =
        createdAclReqs.stream()
            .filter(aclRequest -> allowedEnvIdList.contains(aclRequest.getEnvironment()))
            .collect(Collectors.toList());

    return getAclRequestModelPaged(
        updateCreatAclReqsList(createdAclReqs, tenantId), pageNo, currentPage, tenantId);
  }

  private List<AclRequestsModel> updateCreatAclReqsList(
      List<AclRequests> aclRequestsList, int tenantId) {
    List<AclRequestsModel> aclRequestsModels = getAclRequestsModels(aclRequestsList, tenantId);
    aclRequestsModels =
        aclRequestsModels.stream()
            .sorted(Comparator.comparing(AclRequestsModel::getRequesttime))
            .collect(Collectors.toList());

    return aclRequestsModels;
  }

  public ApiResponse deleteAclRequests(String req_no) throws KlawException {
    try {
      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
        return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
      }
      log.info("deleteAclRequests {}", req_no);
      String result =
          manageDatabase
              .getHandleDbRequests()
              .deleteAclRequest(
                  Integer.parseInt(req_no), commonUtilsService.getTenantId(getUserName()));
      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Exception ", e);
      throw new KlawException(e.getMessage());
    }
  }

  // this will create a delete subscription request
  public ApiResponse createDeleteAclSubscriptionRequest(String req_no) throws KlawException {
    log.info("createDeleteAclSubscriptionRequest {}", req_no);
    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    Acl acl =
        dbHandle.selectSyncAclsFromReqNo(
            Integer.parseInt(req_no), commonUtilsService.getTenantId(getUserName()));

    if (!getEnvsFromUserId(userDetails).contains(acl.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
    }

    AclRequests aclReq = new AclRequests();

    copyProperties(acl, aclReq);
    aclReq.setAcl_ip(acl.getAclip());
    aclReq.setAcl_ssl(acl.getAclssl());

    aclReq.setUsername(userDetails);
    aclReq.setAclType(RequestOperationType.DELETE.value);
    aclReq.setOtherParams(req_no);
    aclReq.setJsonParams(acl.getJsonParams());
    String execRes = manageDatabase.getHandleDbRequests().requestForAcl(aclReq).get("result");

    if (ApiResultStatus.SUCCESS.value.equals(execRes)) {
      mailService.sendMail(
          aclReq.getTopicname(),
          aclReq.getTopictype(),
          "",
          userDetails,
          manageDatabase.getHandleDbRequests(),
          ACL_DELETE_REQUESTED,
          commonUtilsService.getLoginUrl());
    }
    return ApiResponse.builder().result(execRes).build();
  }

  public ApiResponse approveAclRequests(String req_no) throws KlawException {
    log.info("approveAclRequests {}", req_no);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq = dbHandle.selectAcl(Integer.parseInt(req_no), tenantId);

    if (Objects.equals(aclReq.getUsername(), userDetails)) {
      return ApiResponse.builder()
          .result("You are not allowed to approve your own subscription requests.")
          .build();
    }

    if (!RequestStatus.created.name().equals(aclReq.getAclstatus())) {
      return ApiResponse.builder().result("This request does not exist anymore.").build();
    }

    // tenant filtering
    if (!getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    String allIps = aclReq.getAcl_ip();
    String allSsl = aclReq.getAcl_ssl();
    if (aclReq.getReq_no() != null) {
      ResponseEntity<ApiResponse> response = null;
      if (aclReq.getAcl_ip() != null) {
        String[] aclListIp = aclReq.getAcl_ip().split("<ACL>");
        for (String s : aclListIp) {
          aclReq.setAcl_ip(s);
          response = clusterApiService.approveAclRequests(aclReq, tenantId);
        }
      } else if (aclReq.getAcl_ssl() != null) {
        String[] aclListSsl = aclReq.getAcl_ssl().split("<ACL>");
        for (String s : aclListSsl) {
          aclReq.setAcl_ssl(s);
          response = clusterApiService.approveAclRequests(aclReq, tenantId);
        }
      }

      // set back all ips, principles
      aclReq.setAcl_ip(allIps);
      aclReq.setAcl_ssl(allSsl);
      String updateAclReqStatus;

      try {
        ApiResponse responseBody = Objects.requireNonNull(response).getBody();
        if (Objects.requireNonNull(responseBody)
            .getResult()
            .contains(ApiResultStatus.SUCCESS.value)) {
          String jsonParams = "", aivenAclIdKey = "aivenaclid";
          if (responseBody.getData() instanceof Map) {
            Map<String, String> dataMap = (Map<String, String>) responseBody.getData();
            if (dataMap.containsKey(aivenAclIdKey)) {
              jsonParams = "{\"" + aivenAclIdKey + "\":\"" + dataMap.get(aivenAclIdKey) + "\"}";
            }
          }
          updateAclReqStatus = dbHandle.updateAclRequest(aclReq, userDetails, jsonParams);
        } else {
          return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
        }
      } catch (Exception e) {
        log.error("Exception ", e);
        return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
      }

      mailService.sendMail(
          aclReq.getTopicname(),
          aclReq.getTopictype(),
          "",
          aclReq.getUsername(),
          dbHandle,
          ACL_REQUEST_APPROVED,
          commonUtilsService.getLoginUrl());
      return ApiResponse.builder().result(updateAclReqStatus).build();
    } else {
      return ApiResponse.builder().result("Record not found !").build();
    }
  }

  public ApiResponse declineAclRequests(String req_no, String reasonToDecline)
      throws KlawException {
    log.info("declineAclRequests {}", req_no);

    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq =
        dbHandle.selectAcl(Integer.parseInt(req_no), commonUtilsService.getTenantId(getUserName()));

    if (!RequestStatus.created.name().equals(aclReq.getAclstatus())) {
      return ApiResponse.builder().result("This request does not exist anymore.").build();
    }

    // tenant filtering
    if (!getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    String updateAclReqStatus;

    if (aclReq.getReq_no() != null) {
      try {
        updateAclReqStatus = dbHandle.declineAclRequest(aclReq, userDetails);
        mailService.sendMail(
            aclReq.getTopicname(),
            aclReq.getTopictype(),
            reasonToDecline,
            aclReq.getUsername(),
            dbHandle,
            ACL_REQUEST_DENIED,
            commonUtilsService.getLoginUrl());

        return ApiResponse.builder().result(updateAclReqStatus).build();
      } catch (Exception e) {
        log.error("Error ", e);
        throw new KlawException(e.getMessage());
      }
    } else {
      return ApiResponse.builder().result("Record not found !").build();
    }
  }

  private List<Acl> getAclsFromSOT(
      String env, String topicNameSearch, boolean regex, int tenantId) {
    List<Acl> aclsFromSOT;
    if (!regex) {
      aclsFromSOT =
          manageDatabase.getHandleDbRequests().getSyncAcls(env, topicNameSearch, tenantId);
    } else {
      aclsFromSOT = manageDatabase.getHandleDbRequests().getSyncAcls(env, tenantId);
      List<Acl> topicFilteredList = aclsFromSOT;
      // Filter topics on topic name for search
      if (topicNameSearch != null && topicNameSearch.length() > 0) {
        final String topicSearchFilter = topicNameSearch;
        topicFilteredList =
            aclsFromSOT.stream()
                .filter(acl -> acl.getTopicname().contains(topicSearchFilter))
                .collect(Collectors.toList());
      }
      aclsFromSOT = topicFilteredList;
    }

    return aclsFromSOT;
  }

  private Map<String, String> getTopicPromotionEnv(
      String topicSearch, List<Topic> topics, int tenantId) {
    Map<String, String> hashMap = new HashMap<>();
    try {
      if (topics == null) {
        topics = manageDatabase.getHandleDbRequests().getTopics(topicSearch, tenantId);
      }

      hashMap.put("topicName", topicSearch);

      if (topics != null && topics.size() > 0) {
        List<String> envList = new ArrayList<>();
        List<String> finalEnvList = envList;
        topics.forEach(topic -> finalEnvList.add(topic.getEnvironment()));
        envList = finalEnvList;

        // tenant filtering
        String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");
        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        String lastEnv = envList.get(envList.size() - 1);
        List<String> orderdEnvs = Arrays.asList(orderOfEnvs.split(","));

        if (orderdEnvs.indexOf(lastEnv) == orderdEnvs.size() - 1) {
          hashMap.put("status", "NO_PROMOTION"); // PRD
        } else {
          hashMap.put("status", ApiResultStatus.SUCCESS.value);
          hashMap.put("sourceEnv", lastEnv);
          String targetEnv = orderdEnvs.get(orderdEnvs.indexOf(lastEnv) + 1);
          hashMap.put("targetEnv", getEnvDetails(targetEnv, tenantId).getName());
          hashMap.put("targetEnvId", targetEnv);
        }

        return hashMap;
      }
    } catch (Exception e) {
      log.error("getTopicPromotionEnv error ", e);
      hashMap.put("status", ApiResultStatus.FAILURE.value);
      hashMap.put("error", "Topic does not exist in any environment.");
    }

    return hashMap;
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
    } catch (Exception exception) {
      log.error("No environments/clusters found.", exception);
      return new ArrayList<>();
    }
    return topicsFromSOT;
  }

  public TopicOverview getAcls(String topicNameSearch) {
    log.debug("getAcls {}", topicNameSearch);
    String userDetails = getUserName();
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (topicNameSearch != null) {
      topicNameSearch = topicNameSearch.trim();
    } else {
      return null;
    }

    Integer loggedInUserTeam = getMyTeamId(userDetails);
    List<Topic> topics = handleDb.getTopics(topicNameSearch, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = getEnvsFromUserId(userDetails);
    topics =
        topics.stream()
            .filter(topicObj -> allowedEnvIdList.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    TopicOverview topicOverview = new TopicOverview();

    if (topics.size() == 0) {
      topicOverview.setTopicExists(false);
      return topicOverview;
    } else {
      topicOverview.setTopicExists(true);
    }

    String syncCluster;
    String[] reqTopicsEnvs;
    ArrayList<String> reqTopicsEnvsList = new ArrayList<>();
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    } catch (Exception exception) {
      syncCluster = null;
    }

    try {
      String requestTopicsEnvs = mailService.getEnvProperty(tenantId, "REQUEST_TOPICS_OF_ENVS");
      reqTopicsEnvs = requestTopicsEnvs.split(",");
      reqTopicsEnvsList = new ArrayList<>(Arrays.asList(reqTopicsEnvs));
    } catch (Exception exception) {
      log.error("Error in getting req topic envs", exception);
    }

    List<TopicInfo> topicInfoList = new ArrayList<>();
    ArrayList<TopicHistory> topicHistoryFromTopic;
    List<TopicHistory> topicHistoryList = new ArrayList<>();

    for (Topic topic : topics) {
      TopicInfo topicInfo = new TopicInfo();
      topicInfo.setCluster(getEnvDetails(topic.getEnvironment(), tenantId).getName());
      topicInfo.setClusterId(topic.getEnvironment());
      topicInfo.setNoOfPartitions(topic.getNoOfPartitions());
      topicInfo.setNoOfReplcias(topic.getNoOfReplcias());
      topicInfo.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topic.getTeamId()));

      if (syncCluster != null && syncCluster.equals(topic.getEnvironment())) {
        topicOverview.setTopicDocumentation(topic.getDocumentation());
        topicOverview.setTopicIdForDocumentation(topic.getTopicid());
      }

      if (topic.getHistory() != null) {
        try {
          topicHistoryFromTopic =
              OBJECT_MAPPER.readValue(topic.getHistory(), new TypeReference<>() {});
          topicHistoryList.addAll(topicHistoryFromTopic);
        } catch (JsonProcessingException e) {
          log.error("Unable to parse topicHistory ", e);
        }
      }

      topicInfoList.add(topicInfo);
    }

    if (topicOverview.getTopicIdForDocumentation() == null) {
      topicOverview.setTopicDocumentation(topics.get(0).getDocumentation());
      topicOverview.setTopicIdForDocumentation(topics.get(0).getTopicid());
    }

    topicOverview.setTopicHistoryList(topicHistoryList);

    List<Acl> aclsFromSOT = new ArrayList<>();
    List<AclInfo> aclInfo = new ArrayList<>();
    List<AclInfo> tmpAcl;
    List<Acl> prefixedAcls = new ArrayList<>();
    List<Acl> allPrefixedAcls;
    List<AclInfo> tmpAclPrefixed;
    List<AclInfo> prefixedAclsInfo = new ArrayList<>();

    List<Topic> topicsSearchList =
        manageDatabase.getHandleDbRequests().getTopicTeam(topicNameSearch, tenantId);

    // tenant filtering
    Integer topicOwnerTeam = getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();

    for (TopicInfo topicInfo : topicInfoList) {
      aclsFromSOT.addAll(
          getAclsFromSOT(topicInfo.getClusterId(), topicNameSearch, false, tenantId));

      tmpAcl =
          applyFiltersAclsForSOT(loggedInUserTeam, aclsFromSOT, tenantId).stream()
              .collect(Collectors.groupingBy(AclInfo::getTopicname))
              .get(topicNameSearch);

      if (tmpAcl != null) {
        aclInfo.addAll(tmpAcl);
      }

      allPrefixedAcls = handleDb.getPrefixedAclsSOT(topicInfo.getClusterId(), tenantId);
      if (allPrefixedAcls != null && allPrefixedAcls.size() > 0) {
        for (Acl allPrefixedAcl : allPrefixedAcls) {
          if (topicNameSearch.startsWith(allPrefixedAcl.getTopicname())) {
            prefixedAcls.add(allPrefixedAcl);
          }
        }
        tmpAclPrefixed = applyFiltersAclsForSOT(loggedInUserTeam, prefixedAcls, tenantId);
        prefixedAclsInfo.addAll(tmpAclPrefixed);
      }

      // show edit button only for restricted envs
      if (Objects.equals(topicOwnerTeam, loggedInUserTeam)
          && reqTopicsEnvsList.contains(topicInfo.getClusterId())) {
        topicInfo.setShowEditTopic(true);
      }
    }

    aclInfo = aclInfo.stream().distinct().collect(Collectors.toList());
    List<AclInfo> transactionalAcls =
        aclInfo.stream()
            .filter(aclRec -> aclRec.getTransactionalId() != null)
            .collect(Collectors.toList());

    for (AclInfo aclInfo1 : aclInfo) {
      aclInfo1.setEnvironmentName(getEnvDetails(aclInfo1.getEnvironment(), tenantId).getName());
    }

    for (AclInfo aclInfo2 : prefixedAclsInfo) {
      aclInfo2.setEnvironmentName(getEnvDetails(aclInfo2.getEnvironment(), tenantId).getName());
    }

    topicOverview.setAclInfoList(aclInfo);
    if (prefixedAclsInfo.size() > 0) {
      topicOverview.setPrefixedAclInfoList(prefixedAclsInfo);
      topicOverview.setPrefixAclsExists(true);
    }
    if (transactionalAcls.size() > 0) {
      topicOverview.setTransactionalAclInfoList(transactionalAcls);
      topicOverview.setTxnAclsExists(true);
    }

    topicOverview.setTopicInfoList(topicInfoList);

    try {
      if (Objects.equals(topicOwnerTeam, loggedInUserTeam)) {
        topicOverview.setPromotionDetails(getTopicPromotionEnv(topicNameSearch, topics, tenantId));

        if (topicInfoList.size() > 0) {
          TopicInfo lastItem = topicInfoList.get(topicInfoList.size() - 1);
          lastItem.setTopicDeletable(
              aclInfo.stream()
                  .noneMatch(
                      aclItem -> Objects.equals(aclItem.getEnvironment(), lastItem.getCluster())));
          lastItem.setShowDeleteTopic(true);
        }
      } else {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("status", "not_authorized");
        topicOverview.setPromotionDetails(hashMap);
      }
    } catch (Exception e) {
      Map<String, String> hashMap = new HashMap<>();
      hashMap.put("status", "not_authorized");
      topicOverview.setPromotionDetails(hashMap);
    }

    return topicOverview;
  }

  public TopicOverview getSchemaOfTopic(String topicNameSearch, String schemaVersionSearch) {
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    TopicOverview topicOverview = new TopicOverview();
    topicOverview.setTopicExists(true);
    boolean retrieveSchemas = true;
    updateAvroSchema(
        topicNameSearch, schemaVersionSearch, handleDb, retrieveSchemas, topicOverview, tenantId);
    return topicOverview;
  }

  private void updateAvroSchema(
      String topicNameSearch,
      String schemaVersionSearch,
      HandleDbRequests handleDb,
      boolean retrieveSchemas,
      TopicOverview topicOverview,
      int tenantId) {
    if (topicOverview.isTopicExists() && retrieveSchemas) {
      List<Map<String, String>> schemaDetails = new ArrayList<>();
      Map<String, String> schemaMap = new HashMap<>();
      List<Env> schemaEnvs = handleDb.selectAllSchemaRegEnvs(tenantId);
      Object dynamicObj;
      Map<String, Object> hashMapSchemaObj;
      String schemaOfObj;
      for (Env schemaEnv : schemaEnvs) {
        try {
          KwClusters kwClusters =
              manageDatabase
                  .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
                  .get(schemaEnv.getClusterId());
          SortedMap<Integer, Map<String, Object>> schemaObjects =
              clusterApiService.getAvroSchema(
                  kwClusters.getBootstrapServers(),
                  kwClusters.getProtocol(),
                  kwClusters.getClusterName(),
                  topicNameSearch,
                  tenantId);

          Integer latestSchemaVersion = schemaObjects.firstKey();
          Set<Integer> allVersions = schemaObjects.keySet();
          List<Integer> allVersionsList = new ArrayList<>(allVersions);

          try {
            if (schemaVersionSearch != null
                && latestSchemaVersion == Integer.parseInt(schemaVersionSearch)) {
              schemaVersionSearch = "";
            }
          } catch (NumberFormatException ignored) {
          }

          // get latest version
          if (schemaVersionSearch != null && schemaVersionSearch.equals("")) {
            hashMapSchemaObj = schemaObjects.get(latestSchemaVersion);
            schemaOfObj = (String) hashMapSchemaObj.get("schema");
            schemaMap.put("isLatest", "true");
            schemaMap.put("id", hashMapSchemaObj.get("id") + "");
            schemaMap.put("compatibility", hashMapSchemaObj.get("compatibility") + "");
            schemaMap.put("version", "" + latestSchemaVersion);

            if (schemaObjects.size() > 1) {
              schemaMap.put("showNext", "true");
              schemaMap.put("showPrev", "false");
              int indexOfVersion = allVersionsList.indexOf(latestSchemaVersion);
              schemaMap.put("nextVersion", "" + allVersionsList.get(indexOfVersion + 1));
            }
          } else {
            hashMapSchemaObj =
                schemaObjects.get(Integer.parseInt(Objects.requireNonNull(schemaVersionSearch)));
            schemaOfObj = (String) hashMapSchemaObj.get("schema");
            schemaMap.put("isLatest", "false");
            schemaMap.put("id", hashMapSchemaObj.get("id") + "");
            schemaMap.put("compatibility", hashMapSchemaObj.get("compatibility") + "");
            schemaMap.put("version", "" + schemaVersionSearch);

            if (schemaObjects.size() > 1) {
              int indexOfVersion = allVersionsList.indexOf(Integer.parseInt(schemaVersionSearch));
              if (indexOfVersion + 1 == allVersionsList.size()) {
                schemaMap.put("showNext", "false");
                schemaMap.put("showPrev", "true");
                schemaMap.put("prevVersion", "" + allVersionsList.get(indexOfVersion - 1));
              } else {
                schemaMap.put("showNext", "true");
                schemaMap.put("showPrev", "true");
                schemaMap.put("prevVersion", "" + allVersionsList.get(indexOfVersion - 1));
                schemaMap.put("nextVersion", "" + allVersionsList.get(indexOfVersion + 1));
              }
            }
          }

          schemaMap.put("env", schemaEnv.getName());
          dynamicObj = OBJECT_MAPPER.readValue(schemaOfObj, Object.class);
          schemaOfObj = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(dynamicObj);
          schemaMap.put("content", schemaOfObj);

          schemaDetails.add(schemaMap);
          topicOverview.setSchemaExists(true);
          log.info("Getting schema " + topicNameSearch);
        } catch (Exception e) {
          log.error("Error ", e);
        }
      }
      if (topicOverview.isSchemaExists()) topicOverview.setSchemaDetails(schemaDetails);
    }
  }

  private List<AclInfo> applyFiltersAclsForSOT(
      Integer loggedInUserTeam, List<Acl> aclsFromSOT, int tenantId) {

    List<AclInfo> aclList = new ArrayList<>();
    AclInfo mp;

    for (Acl aclSotItem : aclsFromSOT) {
      mp = new AclInfo();
      mp.setEnvironment(aclSotItem.getEnvironment());
      mp.setEnvironmentName(getEnvDetails(aclSotItem.getEnvironment(), tenantId).getName());
      mp.setTopicname(aclSotItem.getTopicname());
      mp.setAcl_ip(aclSotItem.getAclip());
      mp.setAcl_ssl(aclSotItem.getAclssl());
      mp.setTransactionalId(aclSotItem.getTransactionalId());
      mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, aclSotItem.getTeamId()));
      mp.setConsumergroup(aclSotItem.getConsumergroup());
      mp.setTopictype(aclSotItem.getTopictype());
      mp.setAclPatternType(aclSotItem.getAclPatternType());
      mp.setReq_no(aclSotItem.getReq_no() + "");
      if (aclSotItem.getTeamId() != null && aclSotItem.getTeamId().equals(loggedInUserTeam))
        mp.setShowDeleteAcl(true);

      if (aclSotItem.getAclip() != null || aclSotItem.getAclssl() != null) aclList.add(mp);
    }
    return aclList;
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  private boolean validateTeamConsumerGroup(Integer teamId, String consumerGroup, int tenantId) {
    List<Acl> acls = manageDatabase.getHandleDbRequests().getUniqueConsumerGroups(tenantId);

    for (Acl acl : acls) {
      if (!Objects.equals(acl.getTeamId(), teamId)
          && acl.getConsumergroup() != null
          && Objects.equals(acl.getConsumergroup(), consumerGroup)) {
        return true;
      }
    }
    return false;
  }

  public Env getEnvDetails(String envId, int tenantId) {

    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(tenantId).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  // based on tenants
  private List<String> getEnvsFromUserId(String userName) {
    int tenantId = commonUtilsService.getTenantId(userName);
    Integer myTeamId = getMyTeamId(userName);
    return manageDatabase.getTeamsAndAllowedEnvs(myTeamId, tenantId);
  }

  public List<Map<String, String>> getConsumerOffsets(
      String envId, String consumerGroupId, String topicName) {
    List<Map<String, String>> consumerOffsetInfoList = new ArrayList<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    try {
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA, tenantId)
              .get(getEnvDetails(envId, tenantId).getClusterId());
      consumerOffsetInfoList =
          clusterApiService.getConsumerOffsets(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              topicName,
              consumerGroupId,
              tenantId);
    } catch (Exception e) {
      log.error("Ignoring error while retrieving consumer offsets {} ", e.toString());
    }

    return consumerOffsetInfoList;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }
}
