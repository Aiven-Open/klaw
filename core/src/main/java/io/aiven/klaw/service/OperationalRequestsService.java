package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_ERR_101;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.OperationalRequest;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import io.aiven.klaw.model.cluster.consumergroup.OffsetsTiming;
import io.aiven.klaw.model.cluster.consumergroup.ResetConsumerGroupOffsetsRequest;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OperationalRequestsService {

  public static final String OFFSET_RESET_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private final ManageDatabase manageDatabase;
  private final MailUtils mailService;
  private final ClusterApiService clusterApiService;
  private final CommonUtilsService commonUtilsService;

  public OperationalRequestsService(
      ManageDatabase manageDatabase,
      MailUtils mailService,
      ClusterApiService clusterApiService,
      CommonUtilsService commonUtilsService) {
    this.manageDatabase = manageDatabase;
    this.mailService = mailService;
    this.clusterApiService = clusterApiService;
    this.commonUtilsService = commonUtilsService;
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

    ZonedDateTime parsedDate = ZonedDateTime.parse(timeStampStr, dfUTC);
    return Timestamp.from(parsedDate.toInstant());
  }

  public ApiResponse resetConsumerOffsets(String req_no) {
    log.info("approveConsumerOffsetRequests {}", req_no);
    final String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder().build();

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    OperationalRequest operationalRequest =
        dbHandle.getOperationalRequest(Integer.parseInt(req_no), tenantId);
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

    return apiResponse.isSuccess()
        ? ApiResponse.ok(ApiResultStatus.SUCCESS.value)
        : ApiResponse.notOk(ApiResultStatus.FAILURE.value);
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
}
