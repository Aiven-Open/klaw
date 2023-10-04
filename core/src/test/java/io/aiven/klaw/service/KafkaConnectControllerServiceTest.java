package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_KAFKA_CONNECT_ENVS;
import static io.aiven.klaw.service.KafkaConnectControllerService.OBJECT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PromotionStatusType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.KafkaConnectorRequestModel;
import io.aiven.klaw.model.response.ConnectorOverview;
import io.aiven.klaw.model.response.KafkaConnectorRequestsResponseModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jasypt.util.text.BasicTextEncryptor;
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
  public static final String USERNAME = "kwusera";
  public static final String CONNECTOR_NAME = "conn1";
  public static final int TENANT_ID = 101;
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

  @Mock BasicTextEncryptor basicTextEncryptor;

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
        kafkaConnectControllerService, "kafkaConnectorSensitiveFields", "password,username");
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
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TENANT_ID);
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
    when(commonUtilsService.getJasyptEncryptor()).thenReturn(basicTextEncryptor);
    String encryptedText = "encryptedText";
    when(basicTextEncryptor.encrypt(any())).thenReturn(encryptedText);

    ApiResponse apiResponse =
        kafkaConnectControllerService.createConnectorRequest(getConnectRequestModel());
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
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
    assertThat(apiResponse.getMessage())
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
    assertThat(apiResponse.getMessage())
        .isEqualTo("Failure. Invalid config. topics and topics.regex both cannot be configured.");
  }

  @Test
  @Order(5)
  public void createClaimConnectorRequest() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(handleDbRequests.getConnectorsFromName(eq("ConnectorOne"), eq(TENANT_ID)))
        .thenReturn(List.of(getKwKafkaConnector()));
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(envListIds);
    Map<String, String> res = new HashMap<>();
    res.put("result", "success");
    when(handleDbRequests.requestForConnector(any())).thenReturn(res);
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
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(handleDbRequests.getConnectorRequests(
            "ConnectorOne", "1", RequestStatus.CREATED.value, TENANT_ID))
        .thenReturn(List.of(new KafkaConnectorRequest()));
    ApiResponse apiResponse =
        kafkaConnectControllerService.createClaimConnectorRequest("ConnectorOne", "1");

    assertThat(apiResponse.getMessage())
        .isEqualTo("Failure. A request already exists for this connector.");
  }

  @Test
  @Order(7)
  public void getRequests_OrderBy_NEWEST_FIRST() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    when(handleDbRequests.getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(false)))
        .thenReturn(generateKafkaConnectorRequests(50));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            RequestStatus.CREATED,
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
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    when(handleDbRequests.getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(false)))
        .thenReturn(generateKafkaConnectorRequests(50));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            RequestStatus.CREATED,
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

  @Test
  @Order(9)
  public void getRequests_IsOnlyMyRequests() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    when(handleDbRequests.getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(true)))
        .thenReturn(generateKafkaConnectorRequests(50));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            RequestStatus.CREATED,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME,
            null,
            true);

    assertThat(ordered_response).hasSize(10);

    verify(handleDbRequests, times(1))
        .getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(true));
  }

  @Test
  @Order(10)
  public void getRequests_() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    when(handleDbRequests.getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(true)))
        .thenReturn(generateKafkaConnectorRequests(50));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            RequestStatus.CREATED,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME,
            null,
            true);

    assertThat(ordered_response).hasSize(10);

    verify(handleDbRequests, times(1))
        .getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(true));
  }

  @Test
  @Order(11)
  public void getClaimRequests_WhereConnectorIsDeleted() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    List<KafkaConnectorRequest> connectorRequests = generateKafkaConnectorRequests(9);
    connectorRequests.addAll(generateKafkaConnectorRequests(1, 7, RequestOperationType.CLAIM));
    when(handleDbRequests.getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(false)))
        .thenReturn(connectorRequests);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            RequestStatus.CREATED,
            null,
            null,
            io.aiven.klaw.model.enums.Order.DESC_REQUESTED_TIME,
            null,
            false);

    assertThat(ordered_response).hasSize(10);

    Timestamp origReqTime = ordered_response.get(0).getRequesttime();

    for (KafkaConnectorRequestsResponseModel req : ordered_response) {
      if (req.getRequestOperationType().equals(RequestOperationType.CLAIM)) {
        assertThat(req.getRemarks())
            .isEqualTo("This Connector is not found in Klaw. Please contact your Administrator.");
      }
      // assert That each new Request time is older than or equal to the previous request
      assertThat(origReqTime.compareTo(req.getRequesttime()) >= 0).isTrue();
      origReqTime = req.getRequesttime();
    }
  }

  @Test
  @Order(12)
  public void getClaimRequests_WhereConnectorIsNotDeleted() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    List<KafkaConnectorRequest> connectorRequests = generateKafkaConnectorRequests(9);
    connectorRequests.addAll(generateKafkaConnectorRequests(1, 7, RequestOperationType.CLAIM));
    when(handleDbRequests.getAllConnectorRequests(
            anyString(),
            eq(null),
            eq(RequestStatus.CREATED),
            eq(null),
            eq(null),
            eq(TENANT_ID),
            eq(false)))
        .thenReturn(connectorRequests);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getConnectorsFromName(eq("Conn0"), eq(TENANT_ID)))
        .thenReturn(List.of(getKwKafkaConnector()));
    List<KafkaConnectorRequestsResponseModel> ordered_response =
        kafkaConnectControllerService.getConnectorRequests(
            "1",
            "1",
            RequestStatus.CREATED,
            null,
            null,
            io.aiven.klaw.model.enums.Order.DESC_REQUESTED_TIME,
            null,
            false);

    assertThat(ordered_response).hasSize(10);

    Timestamp origReqTime = ordered_response.get(0).getRequesttime();

    for (KafkaConnectorRequestsResponseModel req : ordered_response) {
      if (req.getRequestOperationType().equals(RequestOperationType.CLAIM)) {
        assertThat(req.getRemarks())
            .isNotEqualTo(
                "This Connector is not found in Klaw. Please contact your Administrator.");
      }
      // assert That each new Request time is older than or equal to the previous request
      assertThat(origReqTime.compareTo(req.getRequesttime()) >= 0).isTrue();
      origReqTime = req.getRequesttime();
    }
  }

  @Test
  @Order(13)
  public void getConnectorOverview_WithNoParams_returnsNull() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));

    ConnectorOverview response = kafkaConnectControllerService.getConnectorOverview(null, null);

    assertThat(response).isNull();
  }

  @Test
  @Order(14)
  public void getConnectorOverview_WithAllEnvs_noPromotionOrderSet() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, null);

    assertThat(response.getConnectorInfoList()).hasSize(3);
    assertThat(response.getPromotionDetails().getStatus())
        .isEqualTo(PromotionStatusType.NO_PROMOTION);
    assertThat(response.getAvailableEnvironments()).hasSize(3);
  }

  private List<Env> generateEnvironments() {
    Env dev = new Env();
    dev.setId("0");
    dev.setName("DEV");
    Env tst = new Env();
    tst.setId("1");
    tst.setName("TST");
    Env prd = new Env();
    prd.setId("2");
    prd.setName("PRD");
    return List.of(dev, tst, prd);
  }

  @Test
  @Order(15)
  public void getConnectorOverview_WithAllEnvs() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("2,1,0");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, null);

    assertThat(response.getConnectorInfoList()).hasSize(3);
    assertThat(response.getPromotionDetails().getStatus())
        .isEqualTo(PromotionStatusType.NO_PROMOTION);
    assertThat(response.getAvailableEnvironments()).hasSize(3);
  }

  @Test
  @Order(15)
  public void getConnectorOverview_WithOneEnv() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("2,1,0");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(3));
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList()).hasSize(1);
    assertThat(response.getConnectorInfoList().get(0).getConnectorId()).isEqualTo(1);
    assertThat(response.getPromotionDetails().getStatus())
        .isEqualTo(PromotionStatusType.NO_PROMOTION);
    assertThat(response.getAvailableEnvironments()).hasSize(3);
  }

  @Test
  @Order(15)
  public void getConnectorOverview_WithOneEnvAndPromotion() throws KlawException {
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList()).hasSize(1);
    assertThat(response.getPromotionDetails().getStatus()).isEqualTo(PromotionStatusType.SUCCESS);
    assertThat(response.getPromotionDetails().getTargetEnv()).isEqualTo("PRD");
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  @Test
  @Order(15)
  public void getConnectorOverview_WithOneEnvAndNoPromotionForBaseEnv() throws KlawException {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "0");

    assertThat(response.getConnectorInfoList()).hasSize(1);
    assertThat(response.getPromotionDetails().getStatus())
        .isEqualTo(PromotionStatusType.NO_PROMOTION);
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  @Test
  @Order(16)
  public void updateJsonNodeTestEncryptAndDecrypt() throws JsonProcessingException {
    JsonNode jsonNode = OBJECT_MAPPER.readTree(getValidConnConfig().trim());
    when(commonUtilsService.getJasyptEncryptor()).thenReturn(basicTextEncryptor);
    String encryptedText = "encryptedText";
    when(basicTextEncryptor.encrypt(any())).thenReturn(encryptedText);
    kafkaConnectControllerService.updateJsonNode("encrypt", jsonNode);
    assertThat(jsonNode.get("connector.password").asText()).isEqualTo(encryptedText);
    assertThat(jsonNode.get("jdbc.username").asText()).isEqualTo(encryptedText);
    verify(basicTextEncryptor, times(2)).encrypt(any());

    String decryptedText = "decryptedText";
    when(basicTextEncryptor.decrypt(any())).thenReturn(decryptedText);
    kafkaConnectControllerService.updateJsonNode("decrypt", jsonNode);
    assertThat(jsonNode.get("connector.password").asText()).isEqualTo(decryptedText);
    assertThat(jsonNode.get("jdbc.username").asText()).isEqualTo(decryptedText);
    verify(basicTextEncryptor, times(2)).decrypt(any());

    jsonNode = OBJECT_MAPPER.readTree(getValidConnConfig().trim());
    when(basicTextEncryptor.decrypt(any()))
        .thenThrow(new RuntimeException("NotSupportedOperation"));
    kafkaConnectControllerService.updateJsonNode("decrypt", jsonNode);
    assertThat(jsonNode.get("connector.password").asText()).isEqualTo("testpwd");
  }

  @Test
  @Order(17)
  public void updateJsonNodeTestEncryptAndDecryptNoKeyMatches() throws JsonProcessingException {
    ReflectionTestUtils.setField(
        kafkaConnectControllerService, "kafkaConnectorSensitiveFields", "fielddoesnotexist");

    JsonNode jsonNode = OBJECT_MAPPER.readTree(getValidConnConfig().trim());
    when(commonUtilsService.getJasyptEncryptor()).thenReturn(basicTextEncryptor);
    String encryptedText = "encryptedText";
    when(basicTextEncryptor.encrypt(any())).thenReturn(encryptedText);
    kafkaConnectControllerService.updateJsonNode("encrypt", jsonNode);
    assertThat(jsonNode.get("connector.password").asText()).isEqualTo("testpwd");
    assertThat(jsonNode.get("jdbc.username").asText()).isEqualTo("testuser");

    String decryptedText = "decryptedText";
    when(basicTextEncryptor.decrypt(any())).thenReturn(decryptedText);
    kafkaConnectControllerService.updateJsonNode("decrypt", jsonNode);
    assertThat(jsonNode.get("connector.password").asText()).isEqualTo("testpwd");
    assertThat(jsonNode.get("jdbc.username").asText()).isEqualTo("testuser");

    verify(basicTextEncryptor, times(0)).encrypt(any());
    verify(basicTextEncryptor, times(0)).decrypt(any());
  }

  @Test
  @Order(18)
  public void getConnectorOverview_WithHighestEnvAndConnectorOwnerSet() throws KlawException {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList().get(0).isHighestEnv()).isTrue();
    assertThat(response.getConnectorInfoList().get(0).isConnectorOwner()).isTrue();
    assertThat(response.getPromotionDetails().getStatus()).isEqualTo(PromotionStatusType.SUCCESS);
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  @Test
  @Order(19)
  public void getConnectorOverview_WithRequestsOpen() throws KlawException {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));

    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(
                eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq("1"), eq(101)))
        .thenReturn(true);
    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq(101)))
        .thenReturn(true);
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequest()).isTrue();
    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequestOnAnyEnv()).isTrue();
    assertThat(response.getPromotionDetails().getStatus()).isEqualTo(PromotionStatusType.SUCCESS);
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  @Test
  @Order(20)
  public void getConnectorOverview_WithNoRequestsOpen() throws KlawException {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(
                eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq("1"), eq(101)))
        .thenReturn(false);
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList().get(0).isHighestEnv()).isTrue();
    assertThat(response.getConnectorInfoList().get(0).isConnectorOwner()).isTrue();
    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequest()).isFalse();
    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequestOnAnyEnv()).isFalse();
    assertThat(response.getPromotionDetails().getStatus()).isEqualTo(PromotionStatusType.SUCCESS);
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  @Test
  @Order(21)
  public void getConnectorOverviewPerEnv_ConnectorDoesNotExist() {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));

    when(manageDatabase.getHandleDbRequests().getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(0));

    assertThatThrownBy(
            () ->
                kafkaConnectControllerService.getConnectorDetailsPerEnvToEdit("1", CONNECTOR_NAME))
        .isInstanceOf(KlawBadRequestException.class)
        .hasMessage("Connector conn1 does not exist.");
  }

  @Test
  @Order(22)
  public void getConnectorOverviewPerEnv_ConnectorOwnedByDifferentTeam() {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));

    when(manageDatabase.getHandleDbRequests().getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));

    when(manageDatabase.getHandleDbRequests().getAllTeamsOfUsers(eq(USERNAME), eq(101)))
        .thenReturn(List.of(createTeam("Natto", 23)));

    assertThatThrownBy(
            () ->
                kafkaConnectControllerService.getConnectorDetailsPerEnvToEdit("1", CONNECTOR_NAME))
        .isInstanceOf(KlawBadRequestException.class)
        .hasMessage("Sorry, your team does not own the connector !!");
  }

  // NEW
  @Test
  @Order(23)
  public void getConnectorOverview_WithClaimRequestsOpenAndNoOtherRequestOpen()
      throws KlawException {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));

    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(
                eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq("1"), eq(101)))
        .thenReturn(false);
    when(manageDatabase
            .getHandleDbRequests()
            .existsClaimConnectorRequest(
                eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq(101)))
        .thenReturn(true);
    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq(101)))
        .thenReturn(true);

    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequest()).isTrue();
    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequestOnAnyEnv()).isTrue();
    assertThat(response.getPromotionDetails().getStatus()).isEqualTo(PromotionStatusType.SUCCESS);
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  @Test
  @Order(24)
  public void getConnectorOverview_WithPromotionRequestsOpen() throws KlawException {
    // A promotion is available for the tst connector but we are checking for the dev one and that
    // has already been promoted to tst.
    Set<String> envListIds = new HashSet<>();
    envListIds.add("DEV");
    stubUserInfo();
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTeamId(eq(USERNAME))).thenReturn(8);
    when(handleDbRequests.getConnectors(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));
    when(commonUtilsService.getEnvsFromUserId(eq(USERNAME))).thenReturn(Set.of("0", "1", "2", "3"));
    when(commonUtilsService.getEnvProperty(
            eq(TENANT_ID), eq("REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS")))
        .thenReturn("0,1,2,3");
    when(commonUtilsService.getEnvProperty(eq(TENANT_ID), eq(ORDER_OF_KAFKA_CONNECT_ENVS)))
        .thenReturn("0,1,2");
    when(manageDatabase.getKafkaConnectEnvList(commonUtilsService.getTenantId(eq(USERNAME))))
        .thenReturn(generateEnvironments());
    when(manageDatabase
            .getHandleDbRequests()
            .getConnectorsFromName(eq(CONNECTOR_NAME), eq(TENANT_ID)))
        .thenReturn(generateKafkaConnectors(2));

    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(
                eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq("1"), eq(101)))
        .thenReturn(true);

    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(
                eq(CONNECTOR_NAME),
                eq(RequestStatus.CREATED.value),
                eq(RequestOperationType.PROMOTE.value),
                eq("2"),
                eq(101)))
        .thenReturn(true);
    when(manageDatabase
            .getHandleDbRequests()
            .existsConnectorRequest(eq(CONNECTOR_NAME), eq(RequestStatus.CREATED.value), eq(101)))
        .thenReturn(true);
    ConnectorOverview response =
        kafkaConnectControllerService.getConnectorOverview(CONNECTOR_NAME, "1");

    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequest()).isTrue();
    assertThat(response.getConnectorInfoList().get(0).isHasOpenRequestOnAnyEnv()).isTrue();
    assertThat(response.getPromotionDetails().getStatus())
        .isEqualTo(PromotionStatusType.REQUEST_OPEN);
    assertThat(response.getAvailableEnvironments()).hasSize(2);
  }

  private static Team createTeam(String teamName, int teamId) {
    Team t = new Team();
    t.setTeamId(teamId);
    t.setTeammail(teamName + ".klaw@mailid");
    t.setTeamname(teamName);
    return t;
  }

  private List<KwKafkaConnector> generateKafkaConnectors(int number) {
    List<KwKafkaConnector> connectors = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      KwKafkaConnector conn = new KwKafkaConnector();
      conn.setConnectorId(i);
      conn.setTeamId(8);
      conn.setConnectorName(CONNECTOR_NAME);
      conn.setEnvironment(String.valueOf(i));

      connectors.add(conn);
    }
    return connectors;
  }

  private static List<KafkaConnectorRequest> generateKafkaConnectorRequests(int number) {
    return generateKafkaConnectorRequests(number, 8);
  }

  private static List<KafkaConnectorRequest> generateKafkaConnectorRequests(
      int number, int teamId) {
    return generateKafkaConnectorRequests(number, 8, RequestOperationType.DELETE);
  }

  private static List<KafkaConnectorRequest> generateKafkaConnectorRequests(
      int number, int teamId, RequestOperationType type) {
    List<KafkaConnectorRequest> reqs = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      KafkaConnectorRequest req = new KafkaConnectorRequest();
      req.setConnectorId(i);
      req.setConnectorName("Conn" + i);
      req.setRequesttime(new Timestamp(System.currentTimeMillis() - (3600000 * i)));
      req.setRequestOperationType(type.value);
      req.setRequestStatus("CREATED");
      req.setEnvironment("1");
      req.setEnvironmentName("DEV");
      reqs.add(req);
      req.setTeamId(teamId);
      req.setTenantId(TENANT_ID);
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
    connector.setEnvironment("1");
    connector.setDescription("My Desc");
    connector.setTeamId(8);
    connector.setTenantId(TENANT_ID);
    return connector;
  }

  private String getValidConnConfig() {
    return "{\n"
        + "    \"name\": \"testconn\",\n"
        + "    \"topics\":\"testtopic\",\n"
        + "    \"tasks.max\": \"1\",\n"
        + "    \"connector.password\":\"testpwd\",\n"
        + "    \"jdbc.username\":\"testuser\",\n"
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
    when(userInfo.getTeamId()).thenReturn(TENANT_ID);
    when(mailService.getUserName(any())).thenReturn(USERNAME);
    Env e = new Env();
    e.setId("1");
    e.setName("DEV");
    when(manageDatabase.getKafkaConnectEnvList(anyInt())).thenReturn(List.of(e));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("Octo");
  }
}
