package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.REQ_ERR_101;
import static io.aiven.klaw.helpers.KwConstants.REQUESTOR_SUBSCRIPTIONS;
import static io.aiven.klaw.helpers.UtilMethods.updateEnvStatus;
import static io.aiven.klaw.model.enums.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.ServiceAccounts;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KlawResourceUtils;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.*;
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
import org.springframework.beans.factory.annotation.Qualifier;
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

  @Qualifier("approvalService")
  @Autowired
  private ApprovalService approvalService;

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
      return ApiResponse.NOT_AUTHORIZED;
    }

    String result;
    if (verifyIfTopicExists(aclRequestsModel, tenantId)) {
      return ApiResponse.notOk(ACL_ERR_101);
    }

    String kafkaFlavor =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA, tenantId)
            .get(
                commonUtilsService
                    .getEnvDetails(aclRequestsModel.getEnvironment(), tenantId)
                    .getClusterId())
            .getKafkaFlavor();

    if (AclType.CONSUMER == aclRequestsModel.getAclType()) {
      if (AclPatternType.PREFIXED.value.equals(aclRequestsModel.getAclPatternType())) {
        result = ACL_ERR_102;
        return ApiResponse.notOk(result);
      }

      // ignore consumer group check for Aiven kafka flavors
      if (!kafkaFlavor.equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value)) {
        if (validateIfConsumerGroupUsedByAnotherTeam(
            aclRequestsModel.getRequestingteam(), aclRequestsModel.getConsumergroup(), tenantId)) {
          result = String.format(ACL_ERR_103, aclRequestsModel.getConsumergroup());
          return ApiResponse.notOk(result);
        }
      }
    }

    if (kafkaFlavor.equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value)) {
      if (verifyServiceAccountsOfTeam(aclRequestsModel, tenantId))
        return ApiResponse.notOk(ACL_ERR_104);
    }

    String transactionalId = aclRequestsModel.getTransactionalId();
    if (transactionalId != null && transactionalId.length() > 0) {
      aclRequestsModel.setTransactionalId(transactionalId.trim());
    }

    AclRequests aclRequestsDao = new AclRequests();
    copyProperties(aclRequestsModel, aclRequestsDao);
    aclRequestsDao.setAclType(aclRequestsModel.getAclType().value);
    aclRequestsDao.setRequestOperationType(aclRequestsModel.getRequestOperationType().value);
    if (aclRequestsModel.getRequestId() != null) {
      aclRequestsDao.setReq_no(aclRequestsModel.getRequestId());
    }
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
            userDetails,
            null,
            aclRequestsDao.getTeamId(),
            manageDatabase.getHandleDbRequests(),
            mailType,
            commonUtilsService.getLoginUrl());
        return ApiResponse.ok(execRes);
      }
      return ApiResponse.notOk(execRes);
    } catch (Exception e) {
      log.error("Exception : ", e);
      throw new KlawException(e.getMessage());
    }
  }

  private boolean verifyIfTopicExists(AclRequestsModel aclReq, int tenantId) {
    if (!AclPatternType.LITERAL.value.equals(aclReq.getAclPatternType())) {
      return false;
    }
    List<Topic> topics = commonUtilsService.getTopicsForTopicName(aclReq.getTopicname(), tenantId);

    for (Topic topic : topics) {
      if (Objects.equals(topic.getEnvironment(), aclReq.getEnvironment())) {
        return false;
      }
    }
    return true;
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
    aclReqs =
        Pager.getItemsList(
            pageNo,
            currentPage,
            10,
            aclReqs,
            (pageContext, activityLog) -> {
              activityLog.setAllPageNos(pageContext.getAllPageNos());
              activityLog.setTotalNoPages(pageContext.getTotalPages());
              activityLog.setCurrentPage(pageContext.getPageNo());
              activityLog.setEnvironmentName(
                  commonUtilsService
                      .getEnvDetails(activityLog.getEnvironment(), tenantId)
                      .getName());
              return activityLog;
            });
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

    Set<String> approverRoles =
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
      Set<String> approverRoles,
      String requester,
      int tenantId) {
    List<Topic> topicTeamsList = commonUtilsService.getTopicsForTopicName(topicName, tenantId);
    if (topicTeamsList.size() > 0) {
      Integer teamId = topicTeamsList.get(0).getTeamId();

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

    return Pager.getItemsList(
        pageNo,
        currentPage,
        10,
        updateCreatAclReqsList(createdAclReqs, tenantId, userDetails),
        (pageContext, aclRequestsModel) -> {
          aclRequestsModel.setAllPageNos(pageContext.getAllPageNos());
          aclRequestsModel.setTotalNoPages(pageContext.getTotalPages());
          aclRequestsModel.setCurrentPage(pageContext.getPageNo());
          aclRequestsModel.setTeamname(
              manageDatabase.getTeamNameFromTeamId(tenantId, aclRequestsModel.getTeamId()));
          aclRequestsModel.setEnvironmentName(
              commonUtilsService
                  .getEnvDetails(aclRequestsModel.getEnvironment(), tenantId)
                  .getName());
          return aclRequestsModel;
        });
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
        return ApiResponse.NOT_AUTHORIZED;
      }
      String userName = getCurrentUserName();
      log.info("deleteAclRequests {}", req_no);
      String result =
          manageDatabase
              .getHandleDbRequests()
              .deleteAclRequest(
                  Integer.parseInt(req_no), userName, commonUtilsService.getTenantId(userName));
      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      log.error("Exception ", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse claimAcl(int aclId) throws KlawException {
    log.info("claimAcl {}", aclId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    final String userName = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    // Get ACL
    Optional<Acl> aclOp = manageDatabase.getHandleDbRequests().getAcl(aclId, tenantId);
    if (aclOp.isEmpty()) {
      return ApiResponse.notOk("Acl does not exist.");
    }

    if (manageDatabase
        .getHandleDbRequests()
        .existsAclRequest(
            aclOp.get().getTopicname(),
            RequestStatus.CREATED.value,
            aclOp.get().getEnvironment(),
            tenantId)) {
      return ApiResponse.notOk("A request for this ACL already exists.");
    }

    // Copy into ACL Request
    AclRequests request = new AclRequests();
    copyProperties(aclOp.get(), request);

    // reset the request number
    request.setReq_no(null);
    // Store the original aclId in the other Params section

    request.setAssociatedAclId(aclOp.get().getReq_no());
    // Add Complex Approvers
    request.setRequestingteam(commonUtilsService.getTeamId(userName));
    request.setRequestor(userName);
    request.setRequestOperationType(RequestOperationType.CLAIM.value);
    request.setRequestStatus(RequestStatus.CREATED.value);
    // approvals
    List<Approval> approvals =
        approvalService.getApprovalsForRequest(
            RequestEntityType.ACL,
            RequestOperationType.CLAIM,
            aclOp.get().getEnvironment(),
            aclOp.get().getReq_no(),
            aclOp.get().getTeamId(),
            tenantId);

    request.setApprovals(KlawResourceUtils.approvalsToAclApprovalsList(approvals));

    String res = manageDatabase.getHandleDbRequests().requestForAcl(request).get("result");

    approvalService.sendEmailToApprovers(
        userName, request.getTopicname(), "", null, ACL_REQUESTED, approvals, tenantId);

    return ApiResponse.ok(res);
  }

  // this will create a delete subscription request
  public ApiResponse createDeleteAclSubscriptionRequest(String req_no) throws KlawException {
    log.info("createDeleteAclSubscriptionRequest {}", req_no);
    final String userName = getCurrentUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_SUBSCRIPTIONS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    Acl acl =
        dbHandle.getSyncAclsFromReqNo(
            Integer.parseInt(req_no), commonUtilsService.getTenantId(userName));

    // Verify if user raising request belongs to the same team as the Subscription owner team
    if (!Objects.equals(acl.getTeamId(), commonUtilsService.getTeamId(userName))) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    if (!commonUtilsService.getEnvsFromUserId(userName).contains(acl.getEnvironment())) {
      return ApiResponse.FAILURE;
    }

    // verify if a request already exists
    int tenantId = commonUtilsService.getTenantId(userName);
    List<AclRequests> aclRequests =
        dbHandle.getAllAclRequests(
            false,
            userName,
            REQUESTOR_SUBSCRIPTIONS,
            RequestStatus.CREATED.value,
            false,
            RequestOperationType.DELETE,
            acl.getTopicname(),
            acl.getEnvironment(),
            null,
            AclType.of(acl.getAclType()),
            true,
            tenantId);

    if (!aclRequests.isEmpty()) {
      return ApiResponse.notOk(ACL_ERR_107);
    }

    AclRequests aclRequestsDao = new AclRequests();

    copyProperties(acl, aclRequestsDao);
    aclRequestsDao.setAcl_ip(acl.getAclip());
    aclRequestsDao.setAcl_ssl(acl.getAclssl());
    aclRequestsDao.setRequestor(userName);
    aclRequestsDao.setRequestOperationType(RequestOperationType.DELETE.value);
    aclRequestsDao.setOtherParams(req_no);
    aclRequestsDao.setJsonParams(acl.getJsonParams());
    return executeAclRequestModel(userName, aclRequestsDao, ACL_DELETE_REQUESTED);
  }

  public ApiResponse approveAclRequests(String req_no)
      throws KlawException, KlawBadRequestException {
    log.info("approveAclRequests {}", req_no);
    final String userDetails = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq = dbHandle.getAclRequest(Integer.parseInt(req_no), tenantId);

    if (aclReq.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
      return approveClaimAcl(aclReq, userDetails, tenantId, dbHandle);
    }

    ApiResponse aclValidationResponse = validateAclRequest(aclReq, userDetails, tenantId);
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
    } else {
      updateAuditAndHistory(userDetails, tenantId, dbHandle, aclReq);
    }

    return emailAndReturnClaimUpdate(aclReq, dbHandle, notifyUserType, updateAclReqStatus);
  }

  private void updateAuditAndHistory(
      String userDetails, int tenantId, HandleDbRequests dbHandle, AclRequests aclReq) {
    saveToTopicHistory(userDetails, tenantId, aclReq);
    dbHandle.insertIntoActivityLog(
        RequestEntityType.ACL.value,
        tenantId,
        aclReq.getRequestOperationType(),
        aclReq.getTeamId(),
        aclReq.getTopicname()
            + "-"
            + aclReq.getAclType()
            + (aclReq.getAcl_ip() != null ? aclReq.getAcl_ip() + "-" : "")
            + (aclReq.getAcl_ssl() != null ? aclReq.getAcl_ssl() + "-" : "")
            + (aclReq.getConsumergroup() != null ? aclReq.getConsumergroup() : ""),
        aclReq.getEnvironment(),
        aclReq.getRequestor());
  }

  private ApiResponse approveClaimAcl(
      AclRequests aclReq, String userDetails, int tenantId, HandleDbRequests dbHandle)
      throws KlawException, KlawBadRequestException {
    MailType emailStatus = ACL_REQUEST_APPROVAL_ADDED;
    Optional<Acl> acl =
        manageDatabase.getHandleDbRequests().getAcl(aclReq.getAssociatedAclId(), tenantId);
    if (acl.isEmpty()) {
      return ApiResponse.notOk("Acl no longer exists");
    }
    Optional<Topic> topic =
        manageDatabase.getTopicsForTenant(tenantId).stream()
            .filter(topicSearch -> topicSearch.getTopicname().equals(acl.get().getTopicname()))
            .findFirst();

    if (topic.isEmpty()) {
      return ApiResponse.notOk("Associated Topic to Acl not found exists");
    }
    List<Approval> approvals = KlawResourceUtils.aclApprovalsToApprovalsList(aclReq.getApprovals());

    approvalService.addApproval(
        approvals, userDetails, topic.get().getTeamId(), acl.get().getTeamId());

    boolean fullyApproved = approvalService.isRequestFullyApproved(approvals);
    if (fullyApproved) {
      // changeAclOwnership
      emailStatus = ACL_REQUEST_APPROVED;

      acl.get().setTeamId(aclReq.getRequestingteam());
      manageDatabase.getHandleDbRequests().updateAcl(acl.get());
    }
    aclReq.setApprovals(KlawResourceUtils.approvalsToAclApprovalsList(approvals));
    String status =
        manageDatabase
            .getHandleDbRequests()
            .claimAclRequest(
                aclReq, fullyApproved ? RequestStatus.APPROVED : RequestStatus.CREATED);

    return emailAndReturnClaimUpdate(aclReq, dbHandle, emailStatus, status);
  }

  private ApiResponse emailAndReturnClaimUpdate(
      AclRequests aclReq,
      HandleDbRequests dbHandle,
      MailType notifyUserType,
      String updateAclReqStatus) {
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
    return ApiResultStatus.SUCCESS.value.equals(updateAclReqStatus)
        ? ApiResponse.ok(updateAclReqStatus)
        : ApiResponse.notOk(updateAclReqStatus);
  }

  private void saveToTopicHistory(String userDetails, int tenantId, AclRequests aclReq) {
    String remarksAcl =
        RequestEntityType.ACL.name()
            + " "
            + aclReq.getRequestOperationType()
            + " "
            + aclReq.getAclType();
    if (aclReq.getAclIpPrincipleType().equals(AclIPPrincipleType.IP_ADDRESS)) {
      remarksAcl = remarksAcl + " - " + aclReq.getAcl_ip();
    } else {
      remarksAcl = remarksAcl + " - " + aclReq.getAcl_ssl();
    }

    commonUtilsService.saveTopicHistory(
        aclReq.getRequestOperationType(),
        aclReq.getTopicname(),
        aclReq.getEnvironment(),
        aclReq.getRequestor(),
        aclReq.getRequesttime(),
        aclReq.getRequestingteam(),
        userDetails,
        tenantId,
        RequestEntityType.ACL.name(),
        remarksAcl);
  }

  private ApiResponse validateAclRequest(AclRequests aclReq, String userDetails, int tenantId) {
    if (aclReq == null || aclReq.getReq_no() == null) {
      return ApiResponse.notOk(ACL_ERR_105);
    }

    if (Objects.equals(aclReq.getRequestor(), userDetails)) {
      return ApiResponse.notOk(ACL_ERR_106);
    }

    if (!RequestStatus.CREATED.value.equals(aclReq.getRequestStatus())) {
      return ApiResponse.notOk(REQ_ERR_101);
    }

    if (manageDatabase.getTopicsForTenant(tenantId).stream()
        .noneMatch(
            topic ->
                topic.getEnvironment().equals(aclReq.getEnvironment())
                    && aclReq.getTopicname().equals(topic.getTopicname()))) {
      return ApiResponse.notOk(ACL_ERR_101);
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    return ApiResponse.SUCCESS;
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
      commonUtilsService.updateMetadata(
          tenantId, EntityType.TEAM, MetadataOperationType.UPDATE, null);
    }
  }

  private ResponseEntity<ApiResponse> invokeClusterApiAclRequest(int tenantId, AclRequests aclReq)
      throws KlawException {
    ResponseEntity<ApiResponse> response = null;

    if (aclReq.getAcl_ssl() != null && aclReq.getAcl_ssl().length() > 0) {
      aclReq.setAclIpPrincipleType(AclIPPrincipleType.PRINCIPAL);
    } else {
      aclReq.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
    }
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

    updateEnvStatus(response, manageDatabase, tenantId, aclReq.getEnvironment());

    return response;
  }

  public ApiResponse declineAclRequests(String req_no, String reasonToDecline)
      throws KlawException {
    log.info("declineAclRequests {}", req_no);

    String userDetails = getCurrentUserName();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq =
        dbHandle.getAclRequest(
            Integer.parseInt(req_no), commonUtilsService.getTenantId(userDetails));

    if (aclReq.getReq_no() == null) {
      return ApiResponse.notOk(ACL_ERR_105);
    }

    if (!RequestStatus.CREATED.value.equals(aclReq.getRequestStatus())) {
      return ApiResponse.notOk(REQ_ERR_101);
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userDetails).contains(aclReq.getEnvironment())) {
      return ApiResponse.NOT_AUTHORIZED;
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

      return ApiResultStatus.SUCCESS.value.equals(updateAclReqStatus)
          ? ApiResponse.ok(updateAclReqStatus)
          : ApiResponse.notOk(updateAclReqStatus);
    } catch (Exception e) {
      log.error("Error ", e);
      throw new KlawException(e.getMessage());
    }
  }

  private String getCurrentUserName() {
    return mailService.getCurrentUserName();
  }

  private boolean validateIfConsumerGroupUsedByAnotherTeam(
      Integer teamId, String consumerGroup, int tenantId) {
    return manageDatabase
        .getHandleDbRequests()
        .validateIfConsumerGroupUsedByAnotherTeam(teamId, tenantId, consumerGroup);
  }

  public List<OffsetDetails> getConsumerOffsets(
      String envId, String consumerGroupId, String topicName) {
    List<OffsetDetails> consumerOffsetInfoList = new ArrayList<>();
    int tenantId = commonUtilsService.getTenantId(getCurrentUserName());
    try {
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA, tenantId)
              .get(commonUtilsService.getEnvDetails(envId, tenantId).getClusterId());
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
              .get(commonUtilsService.getEnvDetails(envId, tenantId).getClusterId());
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

  public AclRequestsResponseModel getAclRequest(Integer aclRequestId) {
    String loggedInUser = getCurrentUserName();
    int tenantId = commonUtilsService.getTenantId(loggedInUser);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    AclRequests aclReq = dbHandle.getAclRequest(aclRequestId, tenantId);
    if (aclReq != null) {
      aclReq.setEnvironmentName(
          commonUtilsService.getEnvDetails(aclReq.getEnvironment(), tenantId).getName());
      return getAclRequestsModels(List.of(aclReq), tenantId, loggedInUser).get(0);
    } else {
      return null;
    }
  }
}
