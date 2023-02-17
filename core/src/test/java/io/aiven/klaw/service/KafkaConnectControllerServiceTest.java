package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KafkaConnectorRequestModel;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
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
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");
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

  private KafkaConnectorRequestModel getConnectRequestModel() {
    KafkaConnectorRequestModel kafkaConnectorRequestModel = new KafkaConnectorRequestModel();
    kafkaConnectorRequestModel.setConnectorConfig(getValidConnConfig());
    kafkaConnectorRequestModel.setEnvironment(env.getId());
    kafkaConnectorRequestModel.setRequesttime(new Timestamp(System.currentTimeMillis()));
    kafkaConnectorRequestModel.setRequestOperationType(RequestOperationType.CREATE);

    return kafkaConnectorRequestModel;
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
  }
}
