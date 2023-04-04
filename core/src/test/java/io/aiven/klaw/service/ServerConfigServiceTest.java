package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwPropertiesModel;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.ServerConfigProperties;
import io.aiven.klaw.model.TenantConfig;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ServerConfigServiceTest {

  public static final String KLAW_TENANT_CONFIG = "klaw.tenant.config";
  @Mock private CommonUtilsService commonUtilsService;
  ServerConfigService serverConfigService;

  @Mock private UserDetails userDetails;

  @Mock private MailUtils mailService;
  @Mock private ManageDatabase managedb;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  private Environment env;

  private ObjectMapper mapper = new ObjectMapper();
  @Captor private ArgumentCaptor<KwProperties> propertyCaptor;

  @BeforeEach
  public void setUp() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    this.env = context.getEnvironment();
    loginMock();

    serverConfigService = new ServerConfigService(env, commonUtilsService, mailService, managedb);
  }

  @Test
  @Order(1)
  public void getAllPropsNotAuthorized() {
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(true);
    serverConfigService.getAllProperties();
    List<ServerConfigProperties> list = serverConfigService.getAllProps();
    assertThat(list).isEmpty(); // filtering for spring. and klaw.
  }

  @Test
  @Order(2)
  public void getAllProps() {
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    serverConfigService.getAllProperties();
    List<ServerConfigProperties> list = serverConfigService.getAllProps();
    assertThat(list).isEmpty(); // filtering for spring. and klaw.
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @Order(3)
  public void givenValidTenantModelAllResources_returnSuccess()
      throws KlawException, JsonProcessingException {
    stubValidateTests();
    // Create Test Object
    KwTenantConfigModel prop =
        addKafkaTopicInformation(
            new KwTenantConfigModel(),
            "DEV",
            Arrays.asList("DEV", "TST", "UAT"),
            "DEV",
            "TST",
            "UAT");
    prop =
        addKafkaConnInformation(
            prop,
            Arrays.asList("DEV_CONN", "TST_CONN", "UAT_CONN"),
            "DEV_CONN",
            "TST_CONN",
            "UAT_CONN");

    prop.setTenantName("default");
    TenantConfig config = new TenantConfig();
    config.setTenantModel(prop);
    KwPropertiesModel request =
        createKwPropertiesModel(KLAW_TENANT_CONFIG, mapper.writeValueAsString(config));
    // Execute
    ApiResponse response = serverConfigService.updateKwCustomProperty(request);

    // verify
    assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(1)).updateKwProperty(propertyCaptor.capture(), eq(101));
    KwProperties property = propertyCaptor.getValue();
    assertThat(property.getKwKey()).isEqualTo(KLAW_TENANT_CONFIG);
    TenantConfig tenantConfig = mapper.readValue(property.getKwValue(), TenantConfig.class);

    assertThat(tenantConfig.getTenantModel().getBaseSyncEnvironment()).isEqualTo("1");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(0))
        .isEqualTo("1");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(1))
        .isEqualTo("2");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(2))
        .isEqualTo("3");
  }

  @Test
  @Order(4)
  public void givenValidTenantModelTopicsOnly_returnSuccess()
      throws KlawException, JsonProcessingException {
    stubValidateTests();
    // Create Test Object
    KwTenantConfigModel prop =
        addKafkaTopicInformation(
            new KwTenantConfigModel(),
            "DEV",
            Arrays.asList("DEV", "TST", "UAT"),
            "DEV",
            "TST",
            "UAT");
    prop.setTenantName("default");
    TenantConfig config = new TenantConfig();
    config.setTenantModel(prop);
    KwPropertiesModel request =
        createKwPropertiesModel(KLAW_TENANT_CONFIG, mapper.writeValueAsString(config));
    // Execute
    ApiResponse response = serverConfigService.updateKwCustomProperty(request);

    // verify
    assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(1)).updateKwProperty(propertyCaptor.capture(), eq(101));
    KwProperties property = propertyCaptor.getValue();
    assertThat(property.getKwKey()).isEqualTo(KLAW_TENANT_CONFIG);
    TenantConfig tenantConfig = mapper.readValue(property.getKwValue(), TenantConfig.class);

    assertThat(tenantConfig.getTenantModel().getBaseSyncEnvironment()).isEqualTo("1");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(0))
        .isEqualTo("1");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(1))
        .isEqualTo("2");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(2))
        .isEqualTo("3");
  }

  @Test
  @Order(5)
  public void givenKafkaTopicThatDoesNotExist_returnFailure()
      throws KlawException, JsonProcessingException {
    stubValidateTests();
    // Create Test Object UAT misspelt as UTA in order topic instead of TST_SCH
    KwTenantConfigModel prop =
        addKafkaTopicInformation(
            new KwTenantConfigModel(),
            "DEV",
            Arrays.asList("DEV", "TST", "UTA"),
            "DEV",
            "TST",
            "UAT");
    prop =
        addKafkaConnInformation(
            prop,
            Arrays.asList("DEV_CONN", "TST_CONN", "UAT_CONN"),
            "DEV_CONN",
            "TST_CONN",
            "UAT_CONN");

    prop.setTenantName("default");
    TenantConfig config = new TenantConfig();
    config.setTenantModel(prop);
    KwPropertiesModel request =
        createKwPropertiesModel(KLAW_TENANT_CONFIG, mapper.writeValueAsString(config));
    // Execute
    ApiResponse response = serverConfigService.updateKwCustomProperty(request);

    // verify
    assertThat(response.getMessage()).isNotEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(0)).updateKwProperty(any(), eq(101));
  }

  @Test
  @Order(6)
  public void givenSchemaThatDoesNotExist_returnFailure()
      throws KlawException, JsonProcessingException {
    stubValidateTests();
    // Create Test Object TEST_SCH in order topic instead of TST_SCH
    KwTenantConfigModel prop =
        addKafkaTopicInformation(
            new KwTenantConfigModel(),
            "DEV",
            Arrays.asList("DEV", "TST", "UTA"),
            "DEV",
            "TST",
            "UAT");

    prop =
        addKafkaConnInformation(
            prop,
            Arrays.asList("DEV_CONN", "TST_CONN", "UAT_CONN"),
            "DEV_CONN",
            "TST_CONN",
            "UAT_CONN");
    prop.setTenantName("default");
    TenantConfig config = new TenantConfig();
    config.setTenantModel(prop);
    KwPropertiesModel request =
        createKwPropertiesModel(KLAW_TENANT_CONFIG, mapper.writeValueAsString(config));
    // Execute
    ApiResponse response = serverConfigService.updateKwCustomProperty(request);

    // verify
    assertThat(response.getMessage()).isNotEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(0)).updateKwProperty(any(), eq(101));
    assertThat(response.getMessage())
        .isEqualTo("Failure. Resource UTA must be created before being added to the Tenant Model");
  }

  @Test
  @Order(7)
  public void givenInvalidJson_returnFailure() throws KlawException {
    stubValidateTests();
    KwPropertiesModel request = createKwPropertiesModel(KLAW_TENANT_CONFIG, "{}");
    // Execute
    ApiResponse response = serverConfigService.updateKwCustomProperty(request);

    // verify
    assertThat(response.getMessage()).isNotEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(response.getMessage())
        .isEqualTo("Failure. Please check if the environment names exist.");
  }

  @Test
  @Order(8)
  public void givenValidTenantModelKafkaConnectOnly_returnSuccess()
      throws KlawException, JsonProcessingException {
    stubValidateTests();
    // Create Test Object
    KwTenantConfigModel prop =
        addKafkaConnInformation(
            new KwTenantConfigModel(),
            Arrays.asList("DEV_CONN", "TST_CONN", "UAT_CONN"),
            "DEV_CONN",
            "TST_CONN",
            "UAT_CONN");
    prop.setTenantName("default");
    prop.setBaseSyncKafkaConnectCluster("DEV_CONN");
    TenantConfig config = new TenantConfig();
    config.setTenantModel(prop);
    KwPropertiesModel request =
        createKwPropertiesModel(KLAW_TENANT_CONFIG, mapper.writeValueAsString(config));
    // Execute
    ApiResponse response = serverConfigService.updateKwCustomProperty(request);

    // verify
    assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(1)).updateKwProperty(any(), eq(101));
  }

  @Test
  @Order(9)
  public void givenRequestForConfig_returnCorrectConfig()
      throws KlawException, JsonProcessingException {
    stubValidateTests();

    when(managedb.getKwPropertiesMap(101)).thenReturn(buildFullDbObject());

    // Execute
    List<Map<String, String>> response = serverConfigService.getAllEditableProps();
    TenantConfig tenantConfig =
        mapper.readValue(response.get(0).get("kwvalue"), TenantConfig.class);
    verify(managedb, times(1)).getKwPropertiesMap(101);
    // assert that the order is as specified in getJsonString

    assertThat(tenantConfig.getTenantModel().getBaseSyncEnvironment()).isEqualTo("DEV");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(0))
        .isEqualTo("DEV");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(1))
        .isEqualTo("UAT");
    assertThat(tenantConfig.getTenantModel().getOrderOfTopicPromotionEnvsList().get(2))
        .isEqualTo("TST");

    assertThat(tenantConfig.getTenantModel().getOrderOfConnectorsPromotionEnvsList().get(0))
        .isEqualTo("UAT_CONN");
    assertThat(tenantConfig.getTenantModel().getOrderOfConnectorsPromotionEnvsList().get(1))
        .isEqualTo("TST_CONN");
    assertThat(tenantConfig.getTenantModel().getOrderOfConnectorsPromotionEnvsList().get(2))
        .isEqualTo("DEV_CONN");

    // ensure all parts are correctly being formatted back from the codes.
    assertThat(tenantConfig.getTenantModel().getRequestConnectorsEnvironmentsList().get(0))
        .isEqualTo("TST_CONN");
    assertThat(tenantConfig.getTenantModel().getRequestTopicsEnvironmentsList().get(0))
        .isEqualTo("DEV");
  }

  @Test
  @Order(10)
  public void givenRequestForConfigWithNoneSet_returnCorrectConfig()
      throws KlawException, JsonProcessingException {
    stubValidateTests();
    TenantConfig config = new TenantConfig();
    config.setTenantModel(null);
    Map<String, Map<String, String>> dbObject = new HashMap<>();
    Map<String, String> map = new HashMap();

    map.put("kwvalue", mapper.writeValueAsString(config));
    map.put("kwkey", KLAW_TENANT_CONFIG);
    map.put("kwdes", "Desc");
    map.put("tenantid", "101");
    dbObject.put(KLAW_TENANT_CONFIG, map);

    when(managedb.getKwPropertiesMap(101)).thenReturn(dbObject);

    // Execute
    List<Map<String, String>> response = serverConfigService.getAllEditableProps();
    TenantConfig tenantConfig =
        mapper.readValue(response.get(0).get("kwvalue"), TenantConfig.class);
    verify(managedb, times(1)).getKwPropertiesMap(101);
    // assert that the order is as specified in getJsonString
    assertThat(tenantConfig.getTenantModel()).isNull();
  }

  private Map<String, Map<String, String>> buildFullDbObject() throws JsonProcessingException {
    // This object is created using IDs from stubValidateTests()
    KwTenantConfigModel prop =
        addKafkaTopicInformation(
            new KwTenantConfigModel(), "1", Arrays.asList("1", "2", "3"), "1", "3", "2");
    prop = addKafkaConnInformation(prop, Arrays.asList("5", "6", "4"), "6", "5", "4");

    prop.setTenantName("default");
    TenantConfig config = new TenantConfig();
    config.setTenantModel(prop);
    Map<String, Map<String, String>> dbObject = new HashMap<>();
    Map<String, String> map = new HashMap();

    map.put("kwvalue", mapper.writeValueAsString(config));
    map.put("kwkey", KLAW_TENANT_CONFIG);
    map.put("kwdes", "Desc");
    map.put("tenantid", "101");
    dbObject.put(KLAW_TENANT_CONFIG, map);

    return dbObject;
  }

  private static KwPropertiesModel createKwPropertiesModel(String key, String value) {
    KwPropertiesModel model = new KwPropertiesModel();
    model.setKwKey(key);
    model.setKwValue(value);
    return model;
  }

  private void stubValidateTests() {
    when(mailService.getUserName(any())).thenReturn("test");
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(managedb.getTenantMap())
        .thenReturn(
            new HashMap<Integer, String>() {
              {
                put(101, "default");
              }
            });

    when(managedb.getKafkaEnvList(anyInt()))
        .thenReturn(
            List.of(
                createEnv("DEV", "1", KafkaClustersType.KAFKA.value),
                createEnv("TST", "2", KafkaClustersType.KAFKA.value),
                createEnv("UAT", "3", KafkaClustersType.KAFKA.value)));
    when(managedb.getKafkaConnectEnvList(anyInt()))
        .thenReturn(
            List.of(
                createEnv("DEV_CONN", "4", KafkaClustersType.KAFKA_CONNECT.value),
                createEnv("TST_CONN", "5", KafkaClustersType.KAFKA_CONNECT.value),
                createEnv("UAT_CONN", "6", KafkaClustersType.KAFKA_CONNECT.value)));

    when(managedb.getSchemaRegEnvList(anyInt()))
        .thenReturn(
            List.of(
                createEnv("DEV_SCH", "7", KafkaClustersType.SCHEMA_REGISTRY.value),
                createEnv("TST_SCH", "8", KafkaClustersType.SCHEMA_REGISTRY.value),
                createEnv("UAT_SCH", "9", KafkaClustersType.SCHEMA_REGISTRY.value)));

    when(managedb.getHandleDbRequests()).thenReturn(handleDbRequests);
    when(handleDbRequests.updateKwProperty(any(), eq(101)))
        .thenReturn(ApiResultStatus.SUCCESS.value);
  }

  private Env createEnv(String envName, String envId, String envType) {
    Env env = new Env();
    env.setName(envName);
    env.setTenantId(101);
    env.setId(envId);
    env.setType(envType); // kafka,schemaregistry
    env.setEnvStatus("NOT_KNOWN");
    env.setEnvExists("true");

    return env;
  }

  private KwTenantConfigModel addKafkaTopicInformation(
      KwTenantConfigModel tenantModel,
      String baseSync,
      List<String> topics,
      String... topicsInOrder) {
    tenantModel.setBaseSyncEnvironment(baseSync);
    tenantModel.setOrderOfTopicPromotionEnvsList(List.of(topicsInOrder));
    tenantModel.setRequestTopicsEnvironmentsList(topics);
    return tenantModel;
  }

  private KwTenantConfigModel addKafkaConnInformation(
      KwTenantConfigModel tenantModel, List<String> connectors, String... connectorsInOrder) {
    tenantModel.setOrderOfConnectorsPromotionEnvsList(List.of(connectorsInOrder));
    tenantModel.setRequestConnectorsEnvironmentsList(connectors);
    return tenantModel;
  }
}
