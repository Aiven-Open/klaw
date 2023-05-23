package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.REQ_ERR_101;
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
import io.aiven.klaw.dao.ServiceAccounts;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.OffsetDetails;
import io.aiven.klaw.model.response.ServiceAccountDetails;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AclControllerService {
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String SEPARATOR_ACL = "<ACL>";

  @Value("${klaw.service.accounts.perteam:25}")
  private int allowedServiceAccountsPerTeam;

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
    String currentUserName = getCurrentUserName();
    aclRequestsModel.setRequestOperationType(RequestOperationType.CREATE);
    aclRequestsModel.setRequestor(currentUserName);
    int tenantId = commonUtilsService.getTenantId(currentUserName);
    aclRequestsModel.setRequestingteam(commonUtilsService.getTeamId(currentUserName));

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    String result;
    if (verifyIfTopicExists(aclRequestsModel, tenantId)) {
      return ApiResponse.builder().success(false).message(ACL_ERR_101).build();
    }

    String kafkaFlavor =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA, tenantId)
            .get(getEnvDetails(aclRequestsModel.getEnvironment(), tenantId).getClusterId())
            .getKafkaFlavor();

    if (AclType.CONSUMER == aclRequestsModel.getAclType()) {
      if (AclPatternType.PREFIXED.value.equals(aclRequestsModel.getAclPatternType())) {
        result = ACL_ERR_102;
        return ApiResponse.builder().success(false).message(result).build();
      }

      // ignore consumer group check for Aiven kafka flavors
      if (!kafkaFlavor.equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value)) {
        if (validateTeamConsumerGroup(
            aclRequestsModel.getRequestingteam(), aclRequestsModel.getConsumergroup(), tenantId)) {
          result = String.format(ACL_ERR_103, aclRequestsModel.getConsumergroup());
          return ApiResponse.builder().success(false).message(result).build();
        }
      }
    }

    if (kafkaFlavor.equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value)) {
      if (verifyServiceAccountsOfTeam(aclRequestsModel, tenantId))
        return ApiResponse.builder().success(false).message(ACL_ERR_104).build();
    }

    String transactionalId = aclRequestsModel.getTransactionalId();
    if (transactionalId != null && transactionalId.length() > 0) {
      aclRequestsModel.setTransactionalId(transactionalId.trim());
    }

    AclRequests aclRequestsDao = new AclRequests();
    copyProperties(aclRequestsModel, aclRequestsDao);
    aclRequestsDao.setAclType(aclRequestsModel.getAclType().value);
    aclRequestsDao.setRequestOperationType(aclRequestsModel.getRequestOperationType().value);
    handleIpAddressAndCNString(aclRequestsModel, aclRequestsDao);

    aclRequestsDao.setTenantId(tenantId);
    return executeAclRequestModel(currentUserName, aclRequestsDao, ACL_REQUESTED);
  }

  public boolean verifyServiceAccountsOfTeam(AclRequestsModel aclRequestsModel, int tenantId) {
    // check if service account is from the same team or other team
    boolean isNewServiceAccount = false;

    Optional<Team> optionalTeam =
        manageDatabase.getTeamObjForTenant(tenantId).stream()
            .filter(team -> Objects.equals(team.getTeamId(), aclRequestsModel.getRequestingteam()))
            .findFirst();

    if (optionalTeam.isPresent()) {
      ServiceAccounts serviceAccounts = optionalTeam.get().getServiceAccounts();

      for (String serviceAccountSsl : aclRequestsModel.getAcl_ssl()) {
        if (serviceAccounts != null && serviceAccounts.getServiceAccountsList() != null) {
          if (!serviceAccounts.getServiceAccountsList().contains(serviceAccountSsl)) {
            isNewServiceAccount = true;
          }
        } else {
          if (manageDatabase.getAllServiceAccounts(tenantId).contains(serviceAccountSsl)) {
            return true;
          }
        }
        // service account check if exists in another team
        if (isNewServiceAccount
            && manageDatabase.getAllServiceAccounts(tenantId).contains(serviceAccountSsl)) {
          return true;
        }
      }
    }
    return false;
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
  /*
  TODO ACL can be owned by a different team so make sure correct team is added in there.
   */
  private ApiResponse executeAclRequestModel(
      String userDetails, AclRequests aclRequestsDao, MailType mailType) throws KlawException {
    try {
      String execRes =
          manageDatabase.getHandleDbRequests().requestForAcl(aclRequestsDao).get("result");

      if (ApiResultStatus.SUCCESS.value.equals(execRes)) {
        mailService.sendMail(
            aclRequestsDao.getTopicname(),
            aclRequestsDao.getAclType(),
            "",
            aclRequestsDao.getRequestor(),
            userDetails,
            aclRequestsDao.getTeamId(),
            manageDatabase.getHandleDbRequests(),
            mailType,
            commonUtilsService.getLoginUrl());
        return ApiResponse.builder().success(true).message(execRes).build();
      }
      return ApiResponse.builder().success(false).message(execRes).build();
    } catch (Exception e) {
      log.error("Exception : ", e);
      throw new KlawException(e.getMessage());
    }
  }

  private boolean verifyIfTopicExists(AclRequestsModel aclReq, int tenantId) {
    List<Topic> topics = commonUtilsService.getTopicsForTopicName(aclReq.getTopicname(), tenantId);
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

  public List<AclRequestsResponseModel> getAclRequests(
      String pageNo,
      String currentPage,
      String requestStatus,
      RequestOperationType requestOperationType,
      String topic,
      String env,
      String search,
      AclType aclType,
      Order order,
      boolean isMyRequest) {

    String userName = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    List<AclRequests> aclReqs =
        dbHandle.getAllAclRequests(
            false,
            userName,
            "",
            requestStatus,
            false,
            requestOperationType,
            topic,
            env,
            search,
            aclType,
            isMyRequest,
            tenantId);

    aclReqs = filterAclRequestsByTenantAndOrder(userName, aclReqs, order);
    aclReqs = getAclRequestsPaged(aclReqs, pageNo, currentPage, tenantId);
    return getAclRequestsModels(aclReqs, tenantId, userName);
  }

  private List<AclRequests> filterAclRequestsByTenantAndOrder(
      String userName, List<AclRequests> aclReqs, Order order) {
    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    aclReqs =
        aclReqs.stream()
            .filter(aclRequest -> allowedEnvIdSet.contains(aclRequest.getEnvironment()))
            .sorted(getPreferredOrder(order))
            .collect(Collectors.toList());
    return aclReqs;
  }

  private static Comparator<AclRequests> getPreferredOrder(Order order) {
    return switch (order) {
      case ASC_REQUESTED_TIME -> compareByTime();
      case DESC_REQUESTED_TIME -> Collections.reverseOrder(compareByTime());
    };
  }

  private static Comparator<AclRequests> compareByTime() {
    return Comparator.comparing(AclRequests::getRequesttime);
  }

  private AclRequestsResponseModel setRequestorPermissions(
      AclRequestsResponseModel req, String userName) {
    if (RequestStatus.CREATED == req.getRequestStatus()
        && userName != null
        && userName.equals(req.getRequestor())) {
      req.setDeletable(true);
      req.setEditable(true);
    }

    return req;
  }

  private List<AclRequestsResponseModel> getAclRequestsModels(
      List<AclRequests> aclReqs, int tenantId, String userName) {
    List<AclRequestsResponseModel> aclRequestsModels = new ArrayList<>();
    AclRequestsResponseModel aclRequestsModel;

    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("SUBSCRIPTIONS", tenantId);

    if (aclReqs != null)
      for (AclRequests aclRequests : aclReqs) {
        aclRequestsModel = new AclRequestsResponseModel();
        copyProperties(aclRequests, aclRequestsModel);
        aclRequestsModel.setRequestOperationType(
            RequestOperationType.of(aclRequests.getRequestOperationType()));
        aclRequestsModel.setAclType(AclType.of(aclRequests.getAclType()));
        aclRequestsModel.setRequestStatus(RequestStatus.of(aclRequests.getRequestStatus()));

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
        aclRequestsModel.setRequestingTeamName(
            manageDatabase.getTeamNameFromTeamId(tenantId, aclRequests.getRequestingteam()));

        // show approving info only before approvals
        if (RequestStatus.APPROVED != aclRequestsModel.getRequestStatus()) {
          aclRequestsModel.setApprovingTeamDetails(
              updateApprovingInfo(
                  aclRequestsModel.getTopicname(),
                  aclRequestsModel.getRequestOperationType(),
                  aclRequestsModel.getRequestingteam(),
                  approverRoles,
                  aclRequestsModel.getRequestor(),
                  tenantId));
        }

        aclRequestsModels.add(setRequestorPermissions(aclRequestsModel, userName));
      }
    return aclRequestsModels;
  }

  private String updateApprovingInfo(
      String topicName,
      RequestOperationType requestOperationType,
      Integer team,
      List<String> approverRoles,
      String requester,
      int tenantId) {
    List<Topic> topicTeamsList = commonUtilsService.getTopicsForTopicName(topicName, tenantId);
    if (topicTeamsList.size() > 0) {
      Integer teamId =
          commonUtilsService.getFilteredTopicsForTenant(topicTeamsList).get(0).getTeamId();

      if (RequestOperationType.DELETE == requestOperationType) teamId = team;
      List<UserInfo> userList =
          manageDatabase.getHandleDbRequests().getAllUsersInfoForTeam(teamId, tenantId);

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

  public List<AclRequestsResponseModel> getAclRequestModelPaged(
      List<AclRequestsResponseModel> origActivityList,
      String pageNo,
      String currentPage,
      int tenantId) {
    List<AclRequestsResponseModel> newList = new ArrayList<>();

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
        AclRequestsResponseModel aclRequestsModel = origActivityList.get(i);
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

  public List<AclRequestsResponseModel> getAclRequestsForApprover(
      String pageNo,
      String currentPage,
      String requestStatus,
      String topic,
      String environment,
      RequestOperationType requestOperationType,
      String search,
      AclType aclType,
      Order order) {
    log.debug("getCreatedAclRequests {} {}", pageNo, requestStatus);
    String userDetails = getCurrentUserName();
    List<AclRequests> createdAclReqs;
    int tenantId = commonUtilsService.getTenantId(userDetails);

    // get requests relevant to your teams or all teams
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS)) {
      createdAclReqs =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedAclRequestsByStatus(
                  userDetails,
                  requestStatus,
                  false,
                  requestOperationType,
                  topic,
                  environment,
                  search,
                  aclType,
                  tenantId);
    } else {
      createdAclReqs =
          manageDatabase
              .getHandleDbRequests()
              .getCreatedAclRequestsByStatus(
                  userDetails,
                  requestStatus,
                  true,
                  requestOperationType,
                  topic,
                  environment,
                  search,
                  aclType,
                  tenantId);
    }

    createdAclReqs = filterAclRequestsByTenantAndOrder(getCurrentUserName(), createdAclReqs, order);

    return getAclRequestModelPaged(
        updateCreatAclReqsList(createdAclReqs, tenantId, userDetails),
        pageNo,
        currentPage,
        tenantId);
  }

  private List<AclRequestsResponseModel> updateCreatAclReqsList(
      List<AclRequests> aclRequestsList, int tenantId, String userName) {
    List<AclRequestsResponseModel> aclRequestsModels =
        getAclRequestsModels(aclRequestsList, tenantId, userName);
    aclRequestsModels =
        aclRequestsModels.stream()
            .sorted(Comparator.comparing(AclRequestsResponseModel::getRequesttime))
            .collect(Collectors.toList());

    return aclRequestsModels;
  }

  public ApiResponse deleteAclRequests(String req_no) throws KlawException {
    try {
      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
        return ApiResponse.builder()
            .success(false)
            .message(ApiResultStatus.NOT_AUTHORIZED.value)
            .build();
      }
      String userName = getCurrentUserName();
      log.info("deleteAclRequests {}", req_no);
      String result =
          manageDatabase
              .getHandleDbRequests()
              .deleteAclRequest(
                  Integer.parseInt(req_no), userName, commonUtilsService.getTenantId(userName));
      return ApiResponse.builder()
          .success((result.equals(ApiResultStatus.SUCCESS.value)))
          .message(result)
          .build();
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
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    Acl acl =
        dbHandle.getSyncAclsFromReqNo(
            Integer.parseInt(req_no), commonUtilsService.getTenantId(userDetails));

    // Verify if user raising request belongs to the same team as the Subscription owner team
    if (!Objects.equals(acl.getTeamId(), commonUtilsService.getTeamId(userDetails))) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(acl.getEnvironment())) {
      return ApiResponse.builder().success(false).message(ApiResultStatus.FAILURE.value).build();
    }

    AclRequests aclRequestsDao = new AclRequests();

    copyProperties(acl, aclRequestsDao);
    aclRequestsDao.setAcl_ip(acl.getAclip());
    aclRequestsDao.setAcl_ssl(acl.getAclssl());
    aclRequestsDao.setRequestor(userDetails);
    aclRequestsDao.setRequestOperationType(RequestOperationType.DELETE.value);
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
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq = dbHandle.getAcl(Integer.parseInt(req_no), tenantId);

    ApiResponse aclValidationResponse = validateAclRequest(aclReq, userDetails);
    if (!aclValidationResponse.isSuccess()) {
      return aclValidationResponse;
    }

    String allIps = aclReq.getAcl_ip();
    String allSsl = aclReq.getAcl_ssl();

    ResponseEntity<ApiResponse> response = invokeClusterApiAclRequest(tenantId, aclReq);
    // set back all ips, principals
    aclReq.setAcl_ip(allIps);
    aclReq.setAcl_ssl(allSsl);
    String updateAclReqStatus;
    updateAclReqStatus =
        handleAclRequestClusterApiResponse(userDetails, dbHandle, aclReq, response, tenantId);

    MailType notifyUserType = ACL_REQUEST_APPROVED;
    if (!updateAclReqStatus.equals(ApiResultStatus.SUCCESS.value)) {
      notifyUserType = ACL_REQUEST_FAILURE;
    }

    mailService.sendMail(
        aclReq.getTopicname(),
        aclReq.getAclType(),
        "",
        aclReq.getRequestor(),
        aclReq.getApprover(),
        aclReq.getTeamId(),
        dbHandle,
        notifyUserType,
        commonUtilsService.getLoginUrl());
    return ApiResponse.builder()
        .success((updateAclReqStatus.equals(ApiResultStatus.SUCCESS.value)))
        .message(updateAclReqStatus)
        .build();
  }

  private ApiResponse validateAclRequest(AclRequests aclReq, String userDetails) {
    if (aclReq == null || aclReq.getReq_no() == null) {
      return ApiResponse.builder().success(false).message(ACL_ERR_105).build();
    }

    if (Objects.equals(aclReq.getRequestor(), userDetails)) {
      return ApiResponse.builder().success(false).message(ACL_ERR_106).build();
    }

    if (!RequestStatus.CREATED.value.equals(aclReq.getRequestStatus())) {
      return ApiResponse.builder().success(false).message(REQ_ERR_101).build();
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
  }

  private String handleAclRequestClusterApiResponse(
      String userDetails,
      HandleDbRequests dbHandle,
      AclRequests aclReq,
      ResponseEntity<ApiResponse> response,
      int tenantId) {
    String updateAclReqStatus;
    try {
      ApiResponse responseBody = Objects.requireNonNull(response).getBody();
      if (Objects.requireNonNull(responseBody).isSuccess()) {
        Map<String, String> jsonParams = new HashMap<>();
        String aivenAclIdKey = "aivenaclid";
        Object responseData = responseBody.getData();
        if (responseData instanceof Map) {
          Map<String, String> dataMap = (Map<String, String>) responseData;
          if (dataMap.containsKey(aivenAclIdKey)) {
            jsonParams = dataMap;
            updateServiceAccountsForTeam(aclReq, tenantId);
          }
        }
        updateAclReqStatus = dbHandle.updateAclRequest(aclReq, userDetails, jsonParams, false);
      } else {
        updateAclReqStatus = ApiResultStatus.FAILURE.value;
      }
    } catch (Exception e) {
      log.error("Exception ", e);
      updateAclReqStatus = ApiResultStatus.FAILURE.value;
    }
    return updateAclReqStatus;
  }

  private void updateServiceAccountsForTeam(AclRequests aclRequest, int tenantId) {
    Optional<Team> optionalTeam =
        manageDatabase.getTeamObjForTenant(tenantId).stream()
            .filter(team -> Objects.equals(team.getTeamId(), aclRequest.getRequestingteam()))
            .findFirst();
    if (optionalTeam.isPresent()) {
      ServiceAccounts serviceAccounts = optionalTeam.get().getServiceAccounts();
      if (serviceAccounts != null && serviceAccounts.getServiceAccountsList() != null) {
        serviceAccounts.getServiceAccountsList().add(aclRequest.getAcl_ssl());
      } else {
        serviceAccounts = new ServiceAccounts();
        serviceAccounts.setNumberOfAllowedAccounts(allowedServiceAccountsPerTeam);
        serviceAccounts.setServiceAccountsList(new HashSet<>());
        serviceAccounts.getServiceAccountsList().add(aclRequest.getAcl_ssl());
        optionalTeam.get().setServiceAccounts(serviceAccounts);
      }
      // Update team with service account
      manageDatabase.getHandleDbRequests().updateTeam(optionalTeam.get());
      commonUtilsService.updateMetadata(tenantId, EntityType.TEAM, MetadataOperationType.UPDATE);
    }
  }

  private ResponseEntity<ApiResponse> invokeClusterApiAclRequest(int tenantId, AclRequests aclReq)
      throws KlawException {
    ResponseEntity<ApiResponse> response = null;
    AclIPPrincipleType aclIPPrincipleType = aclReq.getAclIpPrincipleType();
    switch (aclIPPrincipleType) {
      case IP_ADDRESS -> {
        String[] aclListIp = aclReq.getAcl_ip().split(SEPARATOR_ACL);
        for (String s : aclListIp) {
          aclReq.setAcl_ip(s);
          response = clusterApiService.approveAclRequests(aclReq, tenantId);
        }
      }
      case PRINCIPAL -> {
        String[] aclListSsl = aclReq.getAcl_ssl().split(SEPARATOR_ACL);
        for (String s : aclListSsl) {
          aclReq.setAcl_ssl(s);
          response = clusterApiService.approveAclRequests(aclReq, tenantId);
        }
      }
    }

    return response;
  }

  public ApiResponse declineAclRequests(String req_no, String reasonToDecline)
      throws KlawException {
    log.info("declineAclRequests {}", req_no);

    String userDetails = getCurrentUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq =
        dbHandle.getAcl(Integer.parseInt(req_no), commonUtilsService.getTenantId(userDetails));

    if (aclReq.getReq_no() == null) {
      return ApiResponse.builder().success(false).message(ACL_ERR_105).build();
    }

    if (!RequestStatus.CREATED.value.equals(aclReq.getRequestStatus())) {
      return ApiResponse.builder().success(false).message(REQ_ERR_101).build();
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    try {
      String updateAclReqStatus = dbHandle.declineAclRequest(aclReq, userDetails);
      mailService.sendMail(
          aclReq.getTopicname(),
          aclReq.getAclType(),
          reasonToDecline,
          aclReq.getRequestor(),
          aclReq.getApprover(),
          aclReq.getTeamId(),
          dbHandle,
          ACL_REQUEST_DENIED,
          commonUtilsService.getLoginUrl());

      return ApiResponse.builder()
          .success((updateAclReqStatus.equals(ApiResultStatus.SUCCESS.value)))
          .message(updateAclReqStatus)
          .build();
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

  public List<OffsetDetails> getConsumerOffsets(
      String envId, String consumerGroupId, String topicName) {
    List<OffsetDetails> consumerOffsetInfoList = new ArrayList<>();
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

  public ServiceAccountDetails getAivenServiceAccountDetails(
      String envId, String topicName, String serviceAccount, String aclReqNo) {
    String loggedInUser = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(loggedInUser);
    log.info(
        "Retrieving service account details for topic {} serviceAccount {}",
        topicName,
        serviceAccount);
    try {
      HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
      Acl acl =
          dbHandle.getSyncAclsFromReqNo(
              Integer.parseInt(aclReqNo), commonUtilsService.getTenantId(loggedInUser));

      // Verify if loggedInUser belongs to the same team as the Subscription owner team
      if (!Objects.equals(acl.getTeamId(), commonUtilsService.getTeamId(loggedInUser))) {
        return new ServiceAccountDetails();
      }

      // Get details from Cluster Api
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA, tenantId)
              .get(getEnvDetails(envId, tenantId).getClusterId());
      return clusterApiService.getAivenServiceAccountDetails(
          kwClusters.getProjectName(), kwClusters.getServiceName(), serviceAccount, tenantId);
    } catch (Exception e) {
      log.error("Ignoring error while retrieving service account credentials {} ", e.toString());
    }
    return new ServiceAccountDetails();
  }

  public Set<String> getAivenServiceAccounts(String envId) {
    String loggedInUser = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(loggedInUser);
    log.info("Retrieving service accounts for environment {}", envId);
    Set<String> serviceAccountsOfTeam = new HashSet<>();
    try {
      Optional<Team> optionalTeam =
          manageDatabase.getTeamObjForTenant(tenantId).stream()
              .filter(
                  team ->
                      Objects.equals(team.getTeamId(), commonUtilsService.getTeamId(loggedInUser)))
              .findFirst();
      if (optionalTeam.isPresent() && optionalTeam.get().getServiceAccounts() != null) {
        serviceAccountsOfTeam = optionalTeam.get().getServiceAccounts().getServiceAccountsList();
      }
      return serviceAccountsOfTeam;
    } catch (Exception e) {
      log.error("Ignoring error while retrieving service accounts {} ", e.toString());
    }
    return serviceAccountsOfTeam;
  }
}
