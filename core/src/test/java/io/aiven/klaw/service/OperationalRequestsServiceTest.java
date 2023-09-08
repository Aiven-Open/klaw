package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.OP_REQS_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.OP_REQS_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.OP_REQS_ERR_103;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.OperationalRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import io.aiven.klaw.model.cluster.consumergroup.OffsetsTiming;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperationalRequestsServiceTest {

  @Mock private ClusterApiService clusterApiService;

  @Mock private UserDetails userDetails;

  @Mock private UserInfo userInfo;

  @Mock private ManageDatabase manageDatabase;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock CommonUtilsService commonUtilsService;

  @Mock private MailUtils mailService;

  private OperationalRequestsService operationalRequestsService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  @Mock Map<Integer, KwTenantConfigModel> tenantConfig;

  @Mock KwTenantConfigModel tenantConfigModel;

  @Captor ArgumentCaptor<OperationalRequest> operationalRequestArgumentCaptor;
  private Env env;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() throws Exception {
    this.operationalRequestsService =
        new OperationalRequestsService(
            manageDatabase,
            mailService,
            clusterApiService,
            commonUtilsService,
            rolesPermissionsControllerService);
    utilMethods = new UtilMethods();
    this.env = new Env();
    env.setId("1");
    env.setName("DEV");

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @Order(1)
  public void createConsumerOffsetsResetRequestDoesNotOwnGroup() throws KlawNotAuthorizedException {
    ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel =
        utilMethods.getConsumerOffsetResetRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse apiResponse =
        operationalRequestsService.createConsumerOffsetsResetRequest(
            consumerOffsetResetRequestModel);
    assertThat(apiResponse.getMessage()).isEqualTo(OP_REQS_ERR_101);
  }

  @Test
  @Order(2)
  public void createRequestEmptyResetTimeForDateTimeResetType() throws KlawNotAuthorizedException {
    ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel =
        utilMethods.getConsumerOffsetResetRequest(1001);
    consumerOffsetResetRequestModel.setOffsetResetType(OffsetResetType.TO_DATE_TIME);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt(), anyString(), anyInt()))
        .thenReturn(utilMethods.getAcls());
    when(commonUtilsService.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    ApiResponse apiResponse =
        operationalRequestsService.createConsumerOffsetsResetRequest(
            consumerOffsetResetRequestModel);
    assertThat(apiResponse.getMessage()).isEqualTo(OP_REQS_ERR_102);
  }

  @Test
  @Order(3)
  public void createRequestWhichAlreadyExists() throws KlawNotAuthorizedException {
    ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel =
        utilMethods.getConsumerOffsetResetRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt(), anyString(), anyInt()))
        .thenReturn(utilMethods.getAcls());
    when(commonUtilsService.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(mailService.getCurrentUserName()).thenReturn("testuser");
    when(handleDbRequests.getOperationalRequests(
            anyString(),
            any(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(),
            anyBoolean(),
            anyInt()))
        .thenReturn(getReqs());
    ApiResponse apiResponse =
        operationalRequestsService.createConsumerOffsetsResetRequest(
            consumerOffsetResetRequestModel);
    assertThat(apiResponse.getMessage()).isEqualTo(OP_REQS_ERR_103);
  }

  @Test
  @Order(4)
  public void createRequestSuccess() throws KlawNotAuthorizedException {
    ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel =
        utilMethods.getConsumerOffsetResetRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt(), anyString(), anyInt()))
        .thenReturn(utilMethods.getAcls());
    when(commonUtilsService.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getCurrentUserName()).thenReturn("testuser");
    when(handleDbRequests.getOperationalRequests(
            anyString(),
            any(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(),
            anyBoolean(),
            anyInt()))
        .thenReturn(Collections.emptyList());
    Map<String, String> reqStatus = new HashMap<>();
    reqStatus.put("result", ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.requestForConsumerOffsetsReset(any())).thenReturn(reqStatus);
    ApiResponse apiResponse =
        operationalRequestsService.createConsumerOffsetsResetRequest(
            consumerOffsetResetRequestModel);
    assertThat(apiResponse.isSuccess()).isTrue();
  }

  @Test
  @Order(5)
  public void approveOperationalRequestsSuccess() throws KlawException {
    Map<OffsetsTiming, Map<String, Long>> offsetPositionsBeforeAndAfter =
        UtilMethods.getOffsetsTimingMapMap();
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(offsetPositionsBeforeAndAfter).build();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getCurrentUserName()).thenReturn("testuser");
    when(handleDbRequests.getOperationalRequest(anyInt(), anyInt())).thenReturn(getReqs().get(0));
    when(clusterApiService.resetConsumerOffsets(any(), anyString(), anyInt()))
        .thenReturn(apiResponse);
    when(handleDbRequests.updateOperationalChangeRequest(any(), anyString(), any()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse1 = operationalRequestsService.approveOperationalRequests("1001");
    assertThat(apiResponse1.isSuccess()).isTrue();
  }

  @Test
  @Order(6)
  public void approveOperationalRequestsFailure() throws KlawException {
    ApiResponse apiResponse = ApiResponse.builder().success(false).build();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getCurrentUserName()).thenReturn("testuser");
    when(handleDbRequests.getOperationalRequest(anyInt(), anyInt())).thenReturn(getReqs().get(0));
    when(clusterApiService.resetConsumerOffsets(any(), anyString(), anyInt()))
        .thenReturn(apiResponse);

    ApiResponse apiResponse1 = operationalRequestsService.approveOperationalRequests("1001");
    assertThat(apiResponse1.isSuccess()).isFalse();
  }

  private List<OperationalRequest> getReqs() {
    List<OperationalRequest> operationalRequestList = new ArrayList<>();
    OperationalRequest operationalRequest = new OperationalRequest();
    operationalRequest.setConsumerGroup("testgroup");
    operationalRequest.setOperationalRequestType(OperationalRequestType.RESET_CONSUMER_OFFSETS);
    operationalRequest.setTopicname("testtopic");
    operationalRequest.setRequestingTeamId(1001);
    operationalRequest.setRequestStatus(RequestStatus.CREATED.value);
    operationalRequest.setEnvironment("1");
    operationalRequestList.add(operationalRequest);

    return operationalRequestList;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
