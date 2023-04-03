package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.KafkaConnectorRequestModel;
import io.aiven.klaw.model.response.KafkaConnectorRequestsResponseModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaConnectControllerServiceTest {

  @Mock private UserDetails userDetails;

  @Mock private UserInfo userInfo;

  @Mock private ManageDatabase manageDatabase;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock CommonUtilsService commonUtilsService;

  @Mock private MailUtils mailService;

  private KafkaConnectControllerService kafkaConnectControllerService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  @Mock Map<Integer, KwTenantConfigModel> tenantConfig;

  @Mock KwTenantConfigModel tenantConfigModel;

  @Captor ArgumentCaptor<KafkaConnectorRequest> kafkaConnectorRequest;

  private Env env;

  @BeforeEach
  public void setUp() throws Exception {
    this.kafkaConnectControllerService = new KafkaConnectControllerService();
    this.env = new Env();
    env.setId("1");
    env.setName("DEV");
    ReflectionTestUtils.setField(kafkaConnectControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(kafkaConnectControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        kafkaConnectControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        kafkaConnectControllerService,
        "rolesPermissionsControllerService",
        rolesPermissionsControllerService);

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
  public void createConnectorRequest() throws KlawException {
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", ApiResultStatus.SUCCESS.value);

    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncKafkaConnectCluster()).thenReturn("1");
    when(commonUtilsService.getTeamId(anyString())).thenReturn(1);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(commonUtilsService.getEnvProperty(anyInt(), anyString())).thenReturn("1");
    when(handleDbRequests.getConnectorsFromName(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.requestForConnector(any())).thenReturn(resultMap);

    ApiResponse apiResponse =
        kafkaConnectControllerService.createConnectorRequest(getConnectRequestModel());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void createConnectorRequestInvalidJsonConfig() {
    KafkaConnectorRequestModel kafkaConnectorRequestModel = getConnectRequestModel();
    kafkaConnectorRequestModel.setConnectorConfig("plain string"); // Invalid json
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.requestForConnector(any()))
        .thenThrow(new RuntimeException("Unrecognized token"));

    try {
      kafkaConnectControllerService.createConnectorRequest(kafkaConnectorRequestModel);
    } catch (KlawException e) {
      assertThat(e.getMessage()).contains("Unrecognized token");
    }
  }

  @Test
  @Order(3)
  public void createConnectorRequestParameterTopicsDoesntExist() throws KlawException {
    KafkaConnectorRequestModel kafkaConnectorRequestModel = getConnectRequestModel();
    kafkaConnectorRequestModel.setConnectorConfig(getInvalidValidConnConfig());
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse apiResponse =
        kafkaConnectControllerService.createConnectorRequest(kafkaConnectorRequestModel);
    assertThat(apiResponse.getResult())
        .isEqualTo("Failure. Invalid config. topics/topics.regex is not configured");
  }

  // both topics and topics regex are configured which is not correct
  @Test
  @Order(4)
  public void createConnectorRequestParameterTopicsTopicRegexExist() throws KlawException {
    KafkaConnectorRequestModel kafkaConnectorRequestModel = getConnectRequestModel();
    kafkaConnectorRequestModel.setConnectorConfig(getInvalidValidConnConfigTopicsTopicsRegex());
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse apiResponse =
        kafkaConnectControllerService.createConnectorRequest(kafkaConnectorRequestModel);
    assertThat(apiResponse.getResult())
        .isEqualTo("Failure. Invalid config. topics and topics.regex both cannot be configured.");
  }

  @Test
  @Order(5)
  public void createClaimConnectorRequest() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(handleDbRequests.getConnectorsFromName(eq("ConnectorOne"), eq(101)))
        .thenReturn(List.of(getKwKafkaConnector()));
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(envListIds);
    ApiResponse apiResponse =
        kafkaConnectControllerService.createClaimConnectorRequest("ConnectorOne", "1");

    verify(handleDbRequests, times(1)).requestForConnector(kafkaConnectorRequest.capture());
    assertThat(kafkaConnectorRequest.getValue().getApprovingTeamId()).isEqualTo("8");
    assertThat(kafkaConnectorRequest.getValue().getDescription()).isNull();
  }

  @Test
  @Order(6)
  public void createClaimConnectorRequestAlreadyExists() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(handleDbRequests.selectConnectorRequests(
            "ConnectorOne", "1", RequestStatus.CREATED.value, 101))
        .thenReturn(List.of(new KafkaConnectorRequest()));
    ApiResponse apiResponse =
        kafkaConnectControllerService.createClaimConnectorRequest("ConnectorOne", "1");

    assertThat(apiResponse.getResult())
        .isEqualTo("Failure. A request already exists for this connector.");
  }

  @Test
  @Order(7)
  public void getRequests_OrderBy_NEWEST_FIRST() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    when(handleDbRequests.getAllConnectorRequests(
            anyString(), eq(null), eq(null), eq(null), eq(101), eq(false)))
        .thenReturn(generateKafkaConnectorRequests(50));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt()))
        .thenReturn("1", "2");
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            "all",
            null,
            null,
            io.aiven.klaw.model.enums.Order.DESC_REQUESTED_TIME,
            null,
            false);

    assertThat(ordered_response).hasSize(10);

    Timestamp origReqTime = ordered_response.get(0).getRequesttime();

    for (KafkaConnectorRequestsResponseModel req : ordered_response) {

      // assert That each new Request time is older than or equal to the previous request
      assertThat(origReqTime.compareTo(req.getRequesttime()) >= 0).isTrue();
      origReqTime = req.getRequesttime();
    }
  }

  @Test
  @Order(8)
  public void getRequests_OrderBy_OLDEST_FIRST() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    when(handleDbRequests.getAllConnectorRequests(
            anyString(), eq(null), eq(null), eq(null), eq(101), eq(false)))
        .thenReturn(generateKafkaConnectorRequests(50));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt()))
        .thenReturn("1", "2");
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            "all",
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME,
            null,
            false);

    assertThat(ordered_response).hasSize(10);

    Timestamp origReqTime = ordered_response.get(0).getRequesttime();

    for (KafkaConnectorRequestsResponseModel req : ordered_response) {

      // assert That each new Request time is newer than or equal to the previous request
      assertThat(origReqTime.compareTo(req.getRequesttime()) <= 0).isTrue();
      origReqTime = req.getRequesttime();
    }
  }

  private static List<KafkaConnectorRequest> generateKafkaConnectorRequests(int number) {
    List<KafkaConnectorRequest> reqs = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      KafkaConnectorRequest req = new KafkaConnectorRequest();
      req.setConnectorId(i);
      req.setConnectorName("Conn" + i);
      req.setRequesttime(new Timestamp(System.currentTimeMillis() - (3600000 * i)));
      req.setRequestOperationType(RequestOperationType.DELETE.value);
      req.setEnvironment("1");
      req.setEnvironmentName("DEV");
      reqs.add(req);
      req.setTeamId(8);
      req.setTenantId(101);
    }
    return reqs;
  }

  private KafkaConnectorRequestModel getConnectRequestModel() {
    KafkaConnectorRequestModel kafkaConnectorRequestModel = new KafkaConnectorRequestModel();
    kafkaConnectorRequestModel.setConnectorConfig(getValidConnConfig());
    kafkaConnectorRequestModel.setEnvironment(env.getId());
    kafkaConnectorRequestModel.setRequestOperationType(RequestOperationType.CREATE);

    return kafkaConnectorRequestModel;
  }

  private KwKafkaConnector getKwKafkaConnector() {

    KwKafkaConnector connector = new KwKafkaConnector();
    connector.setConnectorId(2);
    connector.setConnectorName("ConnectorOne");
    connector.setEnvironment("DEV");
    connector.setDescription("My Desc");
    connector.setTeamId(8);
    connector.setTenantId(101);
    return connector;
  }

  private String getValidConnConfig() {
    return "{\n"
        + "    \"name\": \"testconn\",\n"
        + "    \"topics\":\"testtopic\",\n"
        + "    \"tasks.max\": \"1\",\n"
        + "    \"connector.class\": \"io.confluent.connect.storage.tools.SchemaSourceConnector\"\n"
        + "}";
  }

  // topic is not a valid attribute here
  private String getInvalidValidConnConfig() {
    return "{\n"
        + "    \"name\": \"testconn\",\n"
        + "    \"topic\":\"testtopic\",\n"
        + "    \"tasks.max\": \"1\",\n"
        + "    \"connector.class\": \"io.confluent.connect.storage.tools.SchemaSourceConnector\"\n"
        + "}";
  }

  // topics and topics.regex both are not allowed in a config
  private String getInvalidValidConnConfigTopicsTopicsRegex() {
    return "{\n"
        + "    \"name\": \"testconn\",\n"
        + "    \"topics\":\"testtopic\",\n"
        + "    \"topics.regex\":\"pattern\",\n"
        + "    \"tasks.max\": \"1\",\n"
        + "    \"connector.class\": \"io.confluent.connect.storage.tools.SchemaSourceConnector\"\n"
        + "}";
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    Env e = new Env();
    e.setId("1");
    e.setName("DEV");
    when(manageDatabase.getKafkaConnectEnvList(anyInt())).thenReturn(List.of(e));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("Octo");
  }
}
