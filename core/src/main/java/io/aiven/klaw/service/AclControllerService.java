package io.aiven.klaw.service;

import static io.aiven.klaw.model.enums.MailType.ACL_DELETE_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUESTED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUEST_APPROVED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUEST_DENIED;
import static io.aiven.klaw.model.enums.MailType.ACL_REQUEST_FAILURE;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclRequestsModel;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  public static final String SEPARATOR_ACL = "<ACL>";
  @Autowired ManageDatabase manageDatabase;

  @Autowired private final MailUtils mailService;

  @Autowired private final ClusterApiService clusterApiService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  @Autowired private CommonUtilsService commonUtilsService;

  AclControllerService(ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public ApiResponse createAcl(AclRequestsModel aclRequestsModel) throws KlawException {
    log.info("createAcl {}", aclRequestsModel);
    String userDetails = getCurrentUserName();
    aclRequestsModel.setAclType(RequestOperationType.CREATE.value);
    aclRequestsModel.setUsername(userDetails);
    int tenantId = commonUtilsService.getTenantId(userDetails);

    aclRequestsModel.setTeamId(
        manageDatabase.getTeamIdFromTeamName(tenantId, aclRequestsModel.getTeamname()));
    aclRequestsModel.setRequestingteam(getMyTeamId(userDetails));

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    String result;
    if (verifyIfTopicExists(aclRequestsModel, tenantId)) {
      return ApiResponse.builder()
          .result("Failure : Topic not found on target environment.")
          .build();
    }

    if (AclType.CONSUMER.value.equals(aclRequestsModel.getTopictype())) {
      if (AclPatternType.PREFIXED.value.equals(aclRequestsModel.getAclPatternType())) {
        result = "Failure : Please change the pattern to LITERAL for topic type.";
        return ApiResponse.builder().result(result).build();
      }

      String kafkaFlavor =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA, tenantId)
              .get(getEnvDetails(aclRequestsModel.getEnvironment(), tenantId).getClusterId())
              .getKafkaFlavor();
      // ignore consumer group check for Aiven kafka flavors
      if (!kafkaFlavor.equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value)) {
        if (validateTeamConsumerGroup(
            aclRequestsModel.getRequestingteam(), aclRequestsModel.getConsumergroup(), tenantId)) {
          result =
              "Failure : Consumer group "
                  + aclRequestsModel.getConsumergroup()
                  + " used by another team.";
          return ApiResponse.builder().result(result).build();
        }
      }
    }

    String transactionalId = aclRequestsModel.getTransactionalId();
    if (transactionalId != null && transactionalId.length() > 0) {
      aclRequestsModel.setTransactionalId(transactionalId.trim());
    }

    AclRequests aclRequestsDao = new AclRequests();
    copyProperties(aclRequestsModel, aclRequestsDao);
    handleIpAddressAndCNString(aclRequestsModel, aclRequestsDao);

    aclRequestsDao.setTenantId(tenantId);
    return executeAclRequestModel(userDetails, aclRequestsDao, ACL_REQUESTED);
  }

  void handleIpAddressAndCNString(AclRequestsModel aclRequestsModel, AclRequests aclRequestsDao) {
    StringBuilder aclStr = new StringBuilder();
    if (aclRequestsModel.getAclIpPrincipleType() == AclIPPrincipleType.IP_ADDRESS) {
      for (int i = 0; i < aclRequestsModel.getAcl_ip().size(); i++) {
        if (i == 0) {
          aclStr.append(aclRequestsModel.getAcl_ip().get(i));
        } else {
          aclStr = new StringBuilder(aclStr + SEPARATOR_ACL + aclRequestsModel.getAcl_ip().get(i));
        }
      }
      aclRequestsDao.setAcl_ip(aclStr.toString());
      aclRequestsDao.setAcl_ssl("User:*");
    } else if (aclRequestsModel.getAclIpPrincipleType() == AclIPPrincipleType.PRINCIPAL) {
      for (int i = 0; i < aclRequestsModel.getAcl_ssl().size(); i++) {
        if (i == 0) {
          aclStr.append(aclRequestsModel.getAcl_ssl().get(i));
        } else {
          aclStr = new StringBuilder(aclStr + SEPARATOR_ACL + aclRequestsModel.getAcl_ssl().get(i));
        }
      }
      aclRequestsDao.setAcl_ssl(aclStr.toString());
      aclRequestsDao.setAcl_ip(null);
    }
  }

  private ApiResponse executeAclRequestModel(
      String userDetails, AclRequests aclRequestsDao, MailType mailType) throws KlawException {
    try {
      String execRes =
          manageDatabase.getHandleDbRequests().requestForAcl(aclRequestsDao).get("result");

      if (ApiResultStatus.SUCCESS.value.equals(execRes)) {
        mailService.sendMail(
            aclRequestsDao.getTopicname(),
            aclRequestsDao.getTopictype(),
            "",
            userDetails,
            manageDatabase.getHandleDbRequests(),
            mailType,
            commonUtilsService.getLoginUrl());
      }
      return ApiResponse.builder().result(execRes).build();
    } catch (Exception e) {
      log.error("Exception : ", e);
      throw new KlawException(e.getMessage());
    }
  }

  private boolean verifyIfTopicExists(AclRequestsModel aclReq, int tenantId) {
    List<Topic> topics =
        manageDatabase.getHandleDbRequests().getTopics(aclReq.getTopicname(), tenantId);
    boolean topicFound = false;

    if (AclPatternType.LITERAL.value.equals(aclReq.getAclPatternType())) {
      for (Topic topic : topics) {
        if (Objects.equals(topic.getEnvironment(), aclReq.getEnvironment())) {
          topicFound = true;
          break;
        }
      }
      return !topicFound;
    }
    return false;
  }

  public List<AclRequestsModel> getAclRequests(
      String pageNo, String currentPage, String requestsType) {

    String userName = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    List<AclRequests> aclReqs =
        dbHandle.getAllAclRequests(false, userName, "", requestsType, false, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(userName);
    aclReqs =
        aclReqs.stream()
            .filter(aclRequest -> allowedEnvIdList.contains(aclRequest.getEnvironment()))
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
          String[] aclListIp = aclRequests.getAcl_ip().split(SEPARATOR_ACL);
          aclRequestsModel.setAcl_ip(new ArrayList<>(Arrays.asList(aclListIp)));
        }

        if (aclRequests.getAcl_ssl() != null) {
          String[] aclListSsl = aclRequests.getAcl_ssl().split(SEPARATOR_ACL);
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
      String requester,
      int tenantId) {
    List<Topic> topicTeamsList =
        manageDatabase.getHandleDbRequests().getTopicTeam(topicName, tenantId);
    if (topicTeamsList.size() > 0) {
      Integer teamId =
          commonUtilsService.getFilteredTopicsForTenant(topicTeamsList).get(0).getTeamId();

      if (RequestOperationType.DELETE.value.equals(aclType)) teamId = team;
      List<UserInfo> userList =
          manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(teamId, tenantId);

      StringBuilder approvingInfo =
          new StringBuilder(
              "Team : " + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ", Users : ");

      for (UserInfo userInfo : userList) {
        if (approverRoles.contains(userInfo.getRole())
            && !Objects.equals(requester, userInfo.getUsername())) {
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
    String userDetails = getCurrentUserName();
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
    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(userDetails);
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
                  Integer.parseInt(req_no), commonUtilsService.getTenantId(getCurrentUserName()));
      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Exception ", e);
      throw new KlawException(e.getMessage());
    }
  }

  // this will create a delete subscription request
  public ApiResponse createDeleteAclSubscriptionRequest(String req_no) throws KlawException {
    log.info("createDeleteAclSubscriptionRequest {}", req_no);
    final String userDetails = getCurrentUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    Acl acl =
        dbHandle.selectSyncAclsFromReqNo(
            Integer.parseInt(req_no), commonUtilsService.getTenantId(userDetails));

    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(acl.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
    }

    AclRequests aclRequestsDao = new AclRequests();

    copyProperties(acl, aclRequestsDao);
    aclRequestsDao.setAcl_ip(acl.getAclip());
    aclRequestsDao.setAcl_ssl(acl.getAclssl());
    aclRequestsDao.setUsername(userDetails);
    aclRequestsDao.setAclType(RequestOperationType.DELETE.value);
    aclRequestsDao.setOtherParams(req_no);
    aclRequestsDao.setJsonParams(acl.getJsonParams());
    return executeAclRequestModel(userDetails, aclRequestsDao, ACL_DELETE_REQUESTED);
  }

  public ApiResponse approveAclRequests(String req_no) throws KlawException {
    log.info("approveAclRequests {}", req_no);
    final String userDetails = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq = dbHandle.selectAcl(Integer.parseInt(req_no), tenantId);

    ApiResponse aclValidationResponse = validateAclRequest(aclReq, userDetails);
    if (!aclValidationResponse.getResult().equals(ApiResultStatus.SUCCESS.value)) {
      return aclValidationResponse;
    }

    String allIps = aclReq.getAcl_ip();
    String allSsl = aclReq.getAcl_ssl();

    ResponseEntity<ApiResponse> response = invokeClusterApiAclRequest(tenantId, aclReq);
    // set back all ips, principals
    aclReq.setAcl_ip(allIps);
    aclReq.setAcl_ssl(allSsl);
    String updateAclReqStatus;
    updateAclReqStatus = handleClusterApiResponse(userDetails, dbHandle, aclReq, response);

    MailType notifyUserType = ACL_REQUEST_APPROVED;
    if (!updateAclReqStatus.equals(ApiResultStatus.SUCCESS.value)) {
      notifyUserType = ACL_REQUEST_FAILURE;
    }

    mailService.sendMail(
        aclReq.getTopicname(),
        aclReq.getTopictype(),
        "",
        aclReq.getUsername(),
        dbHandle,
        notifyUserType,
        commonUtilsService.getLoginUrl());
    return ApiResponse.builder().result(updateAclReqStatus).build();
  }

  private ApiResponse validateAclRequest(AclRequests aclReq, String userDetails) {
    if (aclReq == null || aclReq.getReq_no() == null) {
      return ApiResponse.builder().result("Record not found !").build();
    }

    if (Objects.equals(aclReq.getUsername(), userDetails)) {
      return ApiResponse.builder()
          .result("You are not allowed to approve your own subscription requests.")
          .build();
    }

    if (!RequestStatus.created.name().equals(aclReq.getAclstatus())) {
      return ApiResponse.builder().result("This request does not exist anymore.").build();
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
  }

  private static String handleClusterApiResponse(
      String userDetails,
      HandleDbRequests dbHandle,
      AclRequests aclReq,
      ResponseEntity<ApiResponse> response) {
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
        updateAclReqStatus = ApiResultStatus.FAILURE.value;
      }
    } catch (Exception e) {
      log.error("Exception ", e);
      updateAclReqStatus = ApiResultStatus.FAILURE.value;
    }
    return updateAclReqStatus;
  }

  private ResponseEntity<ApiResponse> invokeClusterApiAclRequest(int tenantId, AclRequests aclReq)
      throws KlawException {
    ResponseEntity<ApiResponse> response = null;
    AclIPPrincipleType aclIPPrincipleType = aclReq.getAclIpPrincipleType();
    switch (aclIPPrincipleType) {
      case IP_ADDRESS:
        String[] aclListIp = aclReq.getAcl_ip().split(SEPARATOR_ACL);
        for (String s : aclListIp) {
          aclReq.setAcl_ip(s);
          response = clusterApiService.approveAclRequests(aclReq, tenantId);
        }
        break;
      case PRINCIPAL:
        String[] aclListSsl = aclReq.getAcl_ssl().split(SEPARATOR_ACL);
        for (String s : aclListSsl) {
          aclReq.setAcl_ssl(s);
          response = clusterApiService.approveAclRequests(aclReq, tenantId);
        }
        break;
    }

    return response;
  }

  public ApiResponse declineAclRequests(String req_no, String reasonToDecline)
      throws KlawException {
    log.info("declineAclRequests {}", req_no);

    String userDetails = getCurrentUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq =
        dbHandle.selectAcl(Integer.parseInt(req_no), commonUtilsService.getTenantId(userDetails));

    if (aclReq.getReq_no() == null) {
      return ApiResponse.builder().result("Record not found !").build();
    }

    if (!RequestStatus.created.name().equals(aclReq.getAclstatus())) {
      return ApiResponse.builder().result("This request does not exist anymore.").build();
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    try {
      String updateAclReqStatus = dbHandle.declineAclRequest(aclReq, userDetails);
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
  }

  private String getCurrentUserName() {
    return mailService.getCurrentUserName();
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

  public List<Map<String, String>> getConsumerOffsets(
      String envId, String consumerGroupId, String topicName) {
    List<Map<String, String>> consumerOffsetInfoList = new ArrayList<>();
    int tenantId = commonUtilsService.getTenantId(getCurrentUserName());
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
