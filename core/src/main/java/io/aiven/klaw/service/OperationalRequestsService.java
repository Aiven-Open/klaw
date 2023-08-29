package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_101;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.OperationalRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import io.aiven.klaw.model.cluster.consumergroup.OffsetsTiming;
import io.aiven.klaw.model.cluster.consumergroup.ResetConsumerGroupOffsetsRequest;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.OperationalRequestsResponseModel;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OperationalRequestsService {

  public static final String OFFSET_RESET_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  private final ManageDatabase manageDatabase;
  private final MailUtils mailService;
  private final ClusterApiService clusterApiService;
  private final CommonUtilsService commonUtilsService;
  private final RolesPermissionsControllerService rolesPermissionsControllerService;

  public OperationalRequestsService(
      ManageDatabase manageDatabase,
      MailUtils mailService,
      ClusterApiService clusterApiService,
      CommonUtilsService commonUtilsService,
      RolesPermissionsControllerService rolesPermissionsControllerService) {
    this.manageDatabase = manageDatabase;
    this.mailService = mailService;
    this.clusterApiService = clusterApiService;
    this.commonUtilsService = commonUtilsService;
    this.rolesPermissionsControllerService = rolesPermissionsControllerService;
  }

  public ApiResponse createConsumerOffsetsResetRequest(
      ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel)
      throws KlawNotAuthorizedException {
    log.info("createConsumerOffsetsResetRequest {}", consumerOffsetResetRequestModel);
    checkIsAuthorized(PermissionType.REQUEST_CREATE_SUBSCRIPTIONS);
    String userName = getUserName();
    consumerOffsetResetRequestModel.setRequestor(userName);
    consumerOffsetResetRequestModel.setRequestingTeamId(commonUtilsService.getTeamId(userName));

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    OperationalRequest operationalRequest = new OperationalRequest();
    copyProperties(consumerOffsetResetRequestModel, operationalRequest);
    operationalRequest.setTenantId(commonUtilsService.getTenantId(userName));
    if (operationalRequest.getOffsetResetType() == OffsetResetType.TO_DATE_TIME) {
      operationalRequest.setResetTimeStamp(
          getUTCTimeStamp(consumerOffsetResetRequestModel.getResetTimeStampStr()));
    }
    String result = dbHandle.requestForConsumerOffsetsReset(operationalRequest).get("result");

    String requestFormattedStr = "Consumer group : " + operationalRequest.getConsumerGroup();
    mailService.sendMail(
        operationalRequest.getTopicname(),
        requestFormattedStr,
        "",
        operationalRequest.getRequestor(),
        operationalRequest.getApprover(),
        operationalRequest.getRequestingTeamId(),
        dbHandle,
        MailType.RESET_CONSUMER_OFFSET_REQUESTED,
        commonUtilsService.getLoginUrl());

    return ApiResultStatus.SUCCESS.value.equals(result)
        ? ApiResponse.ok(result)
        : ApiResponse.notOk(result);
  }

  private Timestamp getUTCTimeStamp(String timeStampStr) {
    DateTimeFormatter df = DateTimeFormatter.ofPattern(OFFSET_RESET_TIMESTAMP_FORMAT);
    DateTimeFormatter dfUTC = df.withZone(ZoneOffset.UTC);

    ZonedDateTime parsedDate = ZonedDateTime.parse(timeStampStr.substring(0, 19), dfUTC);
    return Timestamp.from(parsedDate.toInstant());
  }

  public List<OperationalRequestsResponseModel> getConsumerOffsetsResetRequests(
      String pageNo,
      String currentPage,
      OperationalRequestType operationalRequestType,
      String requestStatus,
      String env,
      String wildcardSearch,
      Order order,
      boolean isMyRequest) {
    log.debug("getTopicRequests page {} operationalRequestType {}", pageNo, operationalRequestType);
    String userName = getUserName();
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    List<OperationalRequest> operationalRequests =
        dbHandle.getOperationalRequests(
            userName,
            operationalRequestType,
            requestStatus,
            env,
            wildcardSearch,
            isMyRequest,
            commonUtilsService.getTenantId(userName));
    // tenant filtering
    operationalRequests = filterByTenantAndSort(order, userName, operationalRequests);

    operationalRequests =
        getOperationalRequestsPaged(
            operationalRequests, pageNo, currentPage, commonUtilsService.getTenantId(userName));
    return getOperationalRequestModels(operationalRequests);
  }

  private List<OperationalRequestsResponseModel> getOperationalRequestModels(
      List<OperationalRequest> operationalRequestList) {
    List<OperationalRequestsResponseModel> operationalRequestModelList = new ArrayList<>();
    OperationalRequestsResponseModel operationalRequestModel;
    String userName = getUserName();
    Integer userTeamId = commonUtilsService.getTeamId(userName);

    int tenantId = commonUtilsService.getTenantId(userName);
    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("TOPICS", tenantId);
    List<UserInfo> userList = manageDatabase.getUsersPerTeamAndTenant(userTeamId, tenantId);

    for (OperationalRequest topicReq : operationalRequestList) {
      operationalRequestModel = new OperationalRequestsResponseModel();
      copyProperties(topicReq, operationalRequestModel);
      operationalRequestModel.setRequestStatus(RequestStatus.of(topicReq.getRequestStatus()));
      operationalRequestModel.setOperationalRequestType(topicReq.getOperationalRequestType());

      operationalRequestModel.setTeamname(
          manageDatabase.getTeamNameFromTeamId(tenantId, topicReq.getRequestingTeamId()));

      // show approving info only before approvals
      if (RequestStatus.APPROVED != operationalRequestModel.getRequestStatus()) {
        operationalRequestModel.setApprovingTeamDetails(
            updateApproverInfo(
                userList,
                manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                approverRoles,
                operationalRequestModel.getRequestor()));
      }
      operationalRequestModelList.add(setRequestorPermissions(operationalRequestModel, userName));
    }
    return operationalRequestModelList;
  }

  private OperationalRequestsResponseModel setRequestorPermissions(
      OperationalRequestsResponseModel req, String userName) {
    log.debug(
        " My request Status {} and userName {} and requestor {}",
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

  private String updateApproverInfo(
      List<UserInfo> userList, String teamName, List<String> approverRoles, String requestor) {
    StringBuilder approvingInfo = new StringBuilder("Team : " + teamName + ", Users : ");

    for (UserInfo userInfo : userList) {
      if (approverRoles.contains(userInfo.getRole())
          && !Objects.equals(requestor, userInfo.getUsername())) {
        approvingInfo.append(userInfo.getUsername()).append(",");
      }
    }

    return approvingInfo.toString();
  }

  private List<OperationalRequest> getOperationalRequestsPaged(
      List<OperationalRequest> origActivityList, String pageNo, String currentPage, int tenantId) {

    List<OperationalRequest> newList = new ArrayList<>();
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

      for (int i = Math.max(0, startVar); i < Math.min(lastVar, totalRecs); i++) {
        OperationalRequest operationalRequest = origActivityList.get(i);
        operationalRequest.setAllPageNos(numList);
        operationalRequest.setTotalNoPages("" + totalPages);
        operationalRequest.setCurrentPage(pageNo);
        envSelected =
            commonUtilsService.getEnvDetails(operationalRequest.getEnvironment(), tenantId);
        operationalRequest.setEnvironmentName(envSelected.getName());
        newList.add(operationalRequest);
      }
    }

    return newList;
  }

  private List<OperationalRequest> filterByTenantAndSort(
      Order order, String userName, List<OperationalRequest> topicReqs) {
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

  private Comparator<OperationalRequest> getPreferredOrdering(Order order) {
    return switch (order) {
      case ASC_REQUESTED_TIME -> compareByTime();
      case DESC_REQUESTED_TIME -> Collections.reverseOrder(compareByTime());
    };
  }

  private static Comparator<OperationalRequest> compareByTime() {
    return Comparator.comparing(OperationalRequest::getRequesttime);
  }

  public ApiResponse approveOperationalRequests(String reqId) {
    log.info("approveConsumerOffsetRequests {}", reqId);
    final String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_OPERATIONAL_REQS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder().build();

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    OperationalRequest operationalRequest =
        dbHandle.getOperationalRequest(Integer.parseInt(reqId), tenantId);
    ApiResponse apiResponse;
    try {
      apiResponse =
          clusterApiService.resetConsumerOffsets(
              resetConsumerGroupOffsetsRequest, operationalRequest.getEnvironment(), tenantId);
    } catch (KlawException e) {
      return ApiResponse.notOk(ApiResultStatus.FAILURE.value);
    }

    if (apiResponse.isSuccess()) {
      if (apiResponse.getData() instanceof Map) {
        Map<OffsetsTiming, Map<String, Long>> offsetPositionsBeforeAndAfter =
            (Map) apiResponse.getData();
        String beforeReset =
            "\n\nBefore Offset Reset"
                + offsetPositionsBeforeAndAfter.get(OffsetsTiming.BEFORE_OFFSET_RESET);
        String afterReset =
            "\n\nAfter Offset Reset"
                + offsetPositionsBeforeAndAfter.get(OffsetsTiming.AFTER_OFFSET_RESET);
        String offsetResetDetails =
            resetConsumerGroupOffsetsRequest.getConsumerGroup() + "\n" + beforeReset + afterReset;
        mailService.sendMail(
            operationalRequest.getTopicname(),
            offsetResetDetails,
            "",
            operationalRequest.getRequestor(),
            operationalRequest.getApprover(),
            operationalRequest.getRequestingTeamId(),
            dbHandle,
            MailType.RESET_CONSUMER_OFFSET_APPROVED,
            commonUtilsService.getLoginUrl());
      }
    }

    return apiResponse.isSuccess() ? ApiResponse.SUCCESS : ApiResponse.FAILURE;
  }

  private void checkIsAuthorized(PermissionType permission) throws KlawNotAuthorizedException {
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), permission)) {
      throw new KlawNotAuthorizedException(TOPICS_ERR_101);
    }
  }

  private String getUserName() {
    return mailService.getCurrentUserName();
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public EnvIdInfo validateOffsetRequestDetails(
      String envId, String topicName, String consumerGroup) {
    log.debug("validateOffsetRequestDetails {} {} {}", envId, topicName, consumerGroup);
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    int teamId = commonUtilsService.getTeamId(userName);

    List<Acl> aclList =
        manageDatabase.getHandleDbRequests().getSyncAcls(envId, topicName, tenantId);

    Optional<Acl> aclFound =
        aclList.stream()
            .filter(
                acl ->
                    acl.getTeamId().equals(teamId)
                        && acl.getConsumergroup() != null
                        && acl.getConsumergroup().equals(consumerGroup))
            .findFirst();
    if (aclFound.isPresent()) {
      EnvIdInfo envIdInfo = new EnvIdInfo();
      envIdInfo.setId(envId);
      envIdInfo.setName(commonUtilsService.getEnvDetails(envId, tenantId).getName());
      return envIdInfo;
    } else {
      return null;
    }
  }
}
