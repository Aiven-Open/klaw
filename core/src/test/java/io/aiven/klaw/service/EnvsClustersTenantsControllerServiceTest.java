package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_108;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_KAFKA_CONNECT_ENVS;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
import static io.aiven.klaw.helpers.KwConstants.REQUEST_TOPICS_OF_ENVS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvID;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.KwRolesPermissions;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawValidationException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.KwTenantModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RolesType;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.KwClustersModel;
import io.aiven.klaw.model.response.ClusterInfo;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.EnvModelResponse;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.model.response.EnvUpdatedStatus;
import io.aiven.klaw.model.response.KwClustersModelResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnvsClustersTenantsControllerServiceTest {

  private EnvsClustersTenantsControllerService service;
  @Mock private MailUtils mailService;

  @Mock private CommonUtilsService commonUtilsService;

  @Mock ManageDatabase manageDatabase;

  @Mock private ClusterApiService clusterApiService;

  @Mock private UsersTeamsControllerService usersTeamsControllerService;
  @Mock private HandleDbRequestsJdbc handleDbRequestsJdbc;
  @Mock private DefaultDataService defaultDataService;

  @Captor ArgumentCaptor<Env> envCapture;

  @BeforeEach
  public void setup() {
    service = new EnvsClustersTenantsControllerService();
    ReflectionTestUtils.setField(service, "mailService", mailService);
    ReflectionTestUtils.setField(service, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(service, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(service, "clusterApiService", clusterApiService);
    ReflectionTestUtils.setField(
        service, "usersTeamsControllerService", usersTeamsControllerService);
    ReflectionTestUtils.setField(service, "defaultDataService", defaultDataService);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(handleDbRequestsJdbc.getUsersInfo(any())).thenReturn(buildUserInfo());
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnv() throws KlawException, KlawValidationException {
    EnvModel env = getTestEnvModel(null);
    Env SchemaEnv = generateKafkaEnv("9", "Schema");
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.getEnvDetails(anyString(), anyInt())).thenReturn(SchemaEnv);
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getMessage()).contains("success");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnvNameAlreadyInUse() throws KlawException, KlawValidationException {
    EnvModel env = getTestEnvModel(null);
    when(handleDbRequestsJdbc.getAllEnvs(anyInt()))
        .thenReturn(
            List.of(
                buildEnv("4", 101, "DEV", KafkaClustersType.KAFKA, 4),
                buildEnv("5", 101, "TST", KafkaClustersType.SCHEMA_REGISTRY, 5)));
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(List.of(buildEnv("4", 101, "DEV", KafkaClustersType.KAFKA, 4)));
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getMessage())
        .contains("Failure. Please choose a different name. This environment name already exists.");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnvWithAssociatedEnv() throws KlawException, KlawValidationException {
    EnvModel env = getTestSchemaEnvModel(new EnvTag("1", "Kafka"));
    Env kafkaEnv = generateKafkaEnv("1", "Kafka");
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.getEnvDetails(eq("1"), eq(101)))
        .thenReturn(kafkaEnv)
        .thenReturn(null);
    ApiResponse response = service.addNewEnv(env);
    kafkaEnv.setAssociatedEnv(env.getAssociatedEnv());
    verify(handleDbRequestsJdbc, times(1)).addNewEnv(eq(kafkaEnv));
    // 1 for saving the schema env 1 for updating the kafka env
    verify(handleDbRequestsJdbc, times(2)).addNewEnv(any(Env.class));
    assertThat(response.getMessage()).contains("success");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void updateEnvWithDifferentAssociatedEnv() throws KlawException, KlawValidationException {
    EnvModel env = getTestSchemaEnvModel(new EnvTag("1", "Kafka"));
    Env SchemaEnv = new Env();
    SchemaEnv.setId("9");
    SchemaEnv.setName("DEV_SCH");
    SchemaEnv.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    SchemaEnv.setTenantId(101);
    SchemaEnv.setAssociatedEnv(new EnvTag("2", "Kafka"));

    Env kafkaEnv = generateKafkaEnv("1", "Kafka");
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.getEnvDetails(eq("1"), eq(101)))
        .thenReturn(kafkaEnv)
        .thenReturn(SchemaEnv);
    when(handleDbRequestsJdbc.getEnvDetails(eq("2"), eq(101)))
        .thenReturn(generateKafkaEnv("2", "Kafka"));
    when(handleDbRequestsJdbc.getNextSeqIdAndUpdate(eq(EntityType.ENVIRONMENT.name()), eq(101)))
        .thenReturn(1);
    ApiResponse response = service.addNewEnv(env);
    kafkaEnv.setAssociatedEnv(env.getAssociatedEnv());
    verify(handleDbRequestsJdbc, times(1)).addNewEnv(eq(kafkaEnv));
    // 1 for saving the schema env 1 for updating the kafka env 1 for updating previous kafka env
    verify(handleDbRequestsJdbc, times(3)).addNewEnv(any(Env.class));
    assertThat(response.getMessage()).contains("success");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void updateEnvWithKafkaEnvAlreadyAssociatedWithAnotherEnv() {
    EnvModel env = getTestSchemaEnvModel(new EnvTag("1", "Kafka"));
    Env SchemaEnv = new Env();
    SchemaEnv.setId("9");
    SchemaEnv.setName("DEV_SCH");
    SchemaEnv.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    SchemaEnv.setTenantId(101);
    SchemaEnv.setAssociatedEnv(new EnvTag("2", "Kafka"));
    Env kafkaEnv = generateKafkaEnv("1", "Kafka");
    kafkaEnv.setAssociatedEnv(new EnvTag("2", "TST_SCH"));
    when(handleDbRequestsJdbc.getEnvDetails(eq("1"), eq(101))).thenReturn(kafkaEnv);

    assertThatExceptionOfType(KlawValidationException.class)
        .isThrownBy(
            () -> {
              service.addNewEnv(env);
            });
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addEnvWithoutAssociatedEnv() throws KlawValidationException, KlawException {
    EnvModel env = getTestSchemaEnvModel(null);
    Env env1 = generateKafkaEnv("1", "Kafka");
    env1.setType(KafkaClustersType.SCHEMA_REGISTRY.value);

    when(handleDbRequestsJdbc.getEnvDetails(eq("1"), eq(101))).thenReturn(env1).thenReturn(null);
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse response = service.addNewEnv(env);

    assertThat(response.getMessage()).contains("success");
    verify(handleDbRequestsJdbc, times(1)).addNewEnv(any());
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addEnvRemoveAssociatedEnv() throws KlawValidationException, KlawException {
    EnvModel env = getTestSchemaEnvModel(null);
    Env env1 = generateKafkaEnv("1", "Kafka");
    env1.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    env1.setAssociatedEnv(new EnvTag("2", "TST_SCH"));
    when(handleDbRequestsJdbc.getEnvDetails(eq("1"), eq(101))).thenReturn(env1);
    when(handleDbRequestsJdbc.getEnvDetails(eq("2"), eq(101)))
        .thenReturn(generateKafkaEnv("2", "Kafka"));
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.getNextSeqIdAndUpdate(eq(EntityType.ENVIRONMENT.name()), eq(101)))
        .thenReturn(1);
    ApiResponse response = service.addNewEnv(env);

    assertThat(response.getMessage()).contains("success");
    // 1 time for the env we are saving and 1 time for removing an existing mapping of a kafka env.
    verify(handleDbRequestsJdbc, times(2)).addNewEnv(any(Env.class));
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addEnvRemoveAssociatedEnvIncorrectIdSupplied()
      throws KlawValidationException, KlawException {
    EnvModel env = getTestSchemaEnvModel(null);
    Env env1 = generateKafkaEnv("1", "Kafka");
    env1.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    env1.setAssociatedEnv(new EnvTag("2", "TST_SCH"));
    when(handleDbRequestsJdbc.getEnvDetails(eq("1"), eq(101))).thenReturn(null);
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.getNextSeqIdAndUpdate(eq(EntityType.ENVIRONMENT.name()), eq(101)))
        .thenReturn(1);
    ApiResponse response = service.addNewEnv(env);

    assertThat(response.getMessage()).contains("success");
    // 1 time for the env we are saving and 1 time for removing an existing mapping of a kafka env.
    verify(handleDbRequestsJdbc, times(1)).addNewEnv(any(Env.class));
  }

  @ParameterizedTest(name = "actual={0} / search={1} / pageNo={2} / expectedNumberOfMatches={4}")
  @MethodSource
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getEnvs(KafkaClustersType type, String searchBy, String pageNo, int expectedMatches)
      throws KlawValidationException, KlawException {

    when(commonUtilsService.getEnvProperty(eq(101), eq(ORDER_OF_TOPIC_ENVS))).thenReturn("1,2,3");
    // dependent on the type
    when(manageDatabase.getKafkaEnvList(eq(101)))
        .thenReturn(
            List.of(
                buildEnv("1", 101, "DEV", type, 1),
                buildEnv("2", 101, "TST", type, 2),
                buildEnv("3", 101, "PRD", type, 3)));
    when(manageDatabase.getSchemaRegEnvList(eq(101)))
        .thenReturn(
            List.of(
                buildEnv("1", 101, "DEV", type, 1),
                buildEnv("2", 101, "TST", type, 2),
                buildEnv("3", 101, "PRD", type, 3)));
    when(manageDatabase.getKafkaConnectEnvList(eq(101)))
        .thenReturn(
            List.of(
                buildEnv("1", 101, "DEV", type, 1),
                buildEnv("2", 101, "TST", type, 2),
                buildEnv("3", 101, "PRD", type, 3)));

    when(manageDatabase.getClusters(eq(type), eq(101))).thenReturn(buildClusters(type, 3));
    when(manageDatabase.getTenantMap())
        .thenReturn(
            new HashMap<>() {
              {
                put(101, "");
              }
            });
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    List<EnvModelResponse> response = service.getEnvsPaginated(type, "", pageNo, searchBy);

    assertThat(response).hasSize(expectedMatches);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteTenantUnauthorizedUser1() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse response = service.deleteTenant();

    assertThat(response).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteTenantUnauthorizedUser2() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    ApiResponse response = service.deleteTenant();

    assertThat(response).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteTenantFailed() throws KlawException {
    int tenantId = 102;
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(manageDatabase.getTenantMap())
        .thenReturn(
            new HashMap<>() {
              {
                put(tenantId, "first");
                put(103, "second");
              }
            });
    String message = "failed";
    when(handleDbRequestsJdbc.disableTenant(tenantId)).thenReturn(message);
    UserInfo info = new UserInfo();
    info.setTenantId(tenantId);
    info.setUsername("first user");
    when(handleDbRequestsJdbc.getAllUsersInfo(tenantId)).thenReturn(List.of(info));
    ApiResponse response = service.deleteTenant();

    assertThat(response.getMessage()).contains(message);
    assertThat(response.isSuccess()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteTenant() throws KlawException {
    int tenantId = 102;
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(manageDatabase.getTenantMap())
        .thenReturn(
            new HashMap<>() {
              {
                put(tenantId, "first");
                put(103, "second");
              }
            });
    String message = "success";
    when(handleDbRequestsJdbc.disableTenant(tenantId)).thenReturn(message);
    UserInfo info = new UserInfo();
    info.setTenantId(tenantId);
    info.setUsername("first user");
    when(handleDbRequestsJdbc.getAllUsersInfo(tenantId)).thenReturn(List.of(info));
    ApiResponse response = service.deleteTenant();

    assertThat(response.getMessage()).contains(message);
    assertThat(String.valueOf(response.getData())).contains("first");
    assertThat(response.isSuccess()).isTrue();
    verify(usersTeamsControllerService, times(1)).deleteUser("first user", false);
    verify(handleDbRequestsJdbc, times(1)).deleteAllUsers(tenantId);
    verify(handleDbRequestsJdbc, times(1)).deleteAllTeams(tenantId);
    verify(handleDbRequestsJdbc, times(1)).deleteAllEnvs(tenantId);
    verify(handleDbRequestsJdbc, times(1)).deleteAllClusters(tenantId);
    verify(handleDbRequestsJdbc, times(1)).deleteAllRolesPerms(tenantId);
    verify(handleDbRequestsJdbc, times(1)).deleteAllKwProps(tenantId);
    verify(handleDbRequestsJdbc, times(1)).deleteTxnData(tenantId);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addTenantIdExceedMax() throws KlawException {
    when(handleDbRequestsJdbc.getTenants()).thenReturn(List.of(new KwTenants(), new KwTenants()));
    ReflectionTestUtils.setField(service, "maxNumberOfTenantsCanBeCreated", 1);
    ApiResponse response = service.addTenantId(new KwTenantModel(), true);

    assertThat(response.getMessage()).contains(ENV_CLUSTER_TNT_ERR_108);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addTenantIdUnauthorized() throws KlawException {
    when(handleDbRequestsJdbc.getTenants()).thenReturn(List.of(new KwTenants(), new KwTenants()));
    ReflectionTestUtils.setField(service, "maxNumberOfTenantsCanBeCreated", 100);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse response = service.addTenantId(new KwTenantModel(), true);

    assertThat(response).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addTenantIdInternal() throws KlawException {
    KwTenantModel kwTenantModel = new KwTenantModel();
    kwTenantModel.setTenantName("first tenant");
    KwTenants kwTenant1 = new KwTenants();
    kwTenant1.setTenantName("first tenant");
    kwTenant1.setTenantId(101);
    KwTenants kwTenant2 = new KwTenants();
    kwTenant2.setTenantName("second tenant");
    kwTenant2.setTenantId(102);
    when(handleDbRequestsJdbc.getTenants()).thenReturn(List.of(kwTenant1, kwTenant2));
    ReflectionTestUtils.setField(service, "maxNumberOfTenantsCanBeCreated", 100);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequestsJdbc.addNewTenant(any())).thenReturn("add new tenant");
    ApiResponse response = service.addTenantId(kwTenantModel, false);

    assertThat(response.getMessage()).contains("add new tenant");
    assertThat(response.isSuccess()).isTrue();
    verify(commonUtilsService, times(1))
        .updateMetadata(
            anyInt(), eq(EntityType.TENANT), eq(MetadataOperationType.CREATE), eq(null));
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addTenantIdExternal() throws KlawException {
    KwTenantModel kwTenantModel = new KwTenantModel();
    kwTenantModel.setTenantName("first tenant");
    KwTenants kwTenant1 = new KwTenants();
    kwTenant1.setTenantName("first tenant");
    kwTenant1.setTenantId(101);
    KwTenants kwTenant2 = new KwTenants();
    kwTenant2.setTenantName("second tenant");
    kwTenant2.setTenantId(102);
    when(handleDbRequestsJdbc.getTenants()).thenReturn(List.of(kwTenant1, kwTenant2));
    ReflectionTestUtils.setField(service, "maxNumberOfTenantsCanBeCreated", 100);
    ReflectionTestUtils.setField(service, "kwInstallationType", "kwInstallationType");
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequestsJdbc.addNewTenant(any())).thenReturn("add new tenant");
    List kwProperties = List.of(new KwProperties());
    List kwRolesPermissions = List.of(new KwRolesPermissions());
    when(defaultDataService.createDefaultProperties(101, "")).thenReturn(kwProperties);
    when(defaultDataService.createDefaultRolesPermissions(101, false, "kwInstallationType"))
        .thenReturn(kwRolesPermissions);
    ApiResponse response = service.addTenantId(kwTenantModel, true);

    assertThat(response.getMessage()).contains("add new tenant");
    assertThat(response.isSuccess()).isTrue();
    verify(commonUtilsService, times(1))
        .updateMetadata(101, EntityType.TENANT, MetadataOperationType.CREATE, null);
    verify(handleDbRequestsJdbc, times(1)).insertDefaultKwProperties(kwProperties);
    verify(handleDbRequestsJdbc, times(1)).insertDefaultRolesPermissions(kwRolesPermissions);
    verify(commonUtilsService, times(1))
        .updateMetadata(101, EntityType.ROLES_PERMISSIONS, MetadataOperationType.CREATE, null);
    verify(commonUtilsService, times(1))
        .updateMetadata(101, EntityType.PROPERTIES, MetadataOperationType.CREATE, null);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getUpdateEnvStatusUnknownEnv() {
    int tenantId = 101;
    when(manageDatabase.getAllEnvList(tenantId))
        .thenReturn(
            List.of(
                buildEnv("1", 101, "DEV", KafkaClustersType.KAFKA, 1),
                buildEnv("2", 101, "TST", KafkaClustersType.KAFKA, 2),
                buildEnv("3", 101, "PRD", KafkaClustersType.KAFKA, 3)));

    assertThatExceptionOfType(KlawBadRequestException.class)
        .isThrownBy(
            () -> {
              service.getUpdateEnvStatus("4");
            });
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getUpdateEnvStatus() throws KlawBadRequestException {
    int tenantId = 101;
    Env env1 = buildEnv("1", 101, "PRD", KafkaClustersType.KAFKA, 1);
    Env env2 = buildEnv("2", 101, "PRD", KafkaClustersType.KAFKA, 2);
    Env env3 = buildEnv("3", 101, "PRD", KafkaClustersType.KAFKA, 3);
    when(manageDatabase.getAllEnvList(tenantId)).thenReturn(List.of(env1, env2, env3));
    Map<Integer, KwClusters> clusters = buildClusters(KafkaClustersType.KAFKA, 3);
    KwClusters kwCluster = clusters.get(3);
    when(manageDatabase.getClusters(KafkaClustersType.KAFKA, tenantId)).thenReturn(clusters);
    when(clusterApiService.getKafkaClusterStatus(
            null,
            KafkaSupportedProtocol.SSL,
            "33",
            "kafka",
            KafkaFlavors.APACHE_KAFKA.value,
            tenantId))
        .thenReturn(ClusterStatus.ONLINE);

    EnvUpdatedStatus envUpdatedStatus = service.getUpdateEnvStatus("3");
    assertThat(kwCluster.getClusterStatus()).isEqualTo(ClusterStatus.ONLINE);
    verify(handleDbRequestsJdbc, times(1)).addNewCluster(kwCluster);
    verify(manageDatabase, times(1)).addEnvToCache(tenantId, env3, false);
    assertThat(envUpdatedStatus.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(envUpdatedStatus.getEnvStatus()).isEqualTo(ClusterStatus.ONLINE);
    assertThat(envUpdatedStatus.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvironmentUnauthorized() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse response = service.deleteEnvironment("envId", "envType");

    assertThat(response).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvironmentExistKafkaComponents() throws KlawException {
    int tenantId = 101;
    String envId = "20";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.existsKafkaComponentsForEnv(envId, tenantId)).thenReturn(true);
    ApiResponse response = service.deleteEnvironment(envId, KafkaClustersType.KAFKA.value);

    assertThat(response.getMessage()).contains(ENV_CLUSTER_TNT_ERR_105);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvironmentExistKafkaConnectComponents() throws KlawException {
    int tenantId = 101;
    String envId = "20";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.existsConnectorComponentsForEnv(envId, tenantId)).thenReturn(true);
    ApiResponse response = service.deleteEnvironment(envId, KafkaClustersType.KAFKA_CONNECT.value);

    assertThat(response.getMessage()).contains(ENV_CLUSTER_TNT_ERR_106);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvironmentExistSchemaRegistryComponents() throws KlawException {
    int tenantId = 101;
    String envId = "20";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.existsSchemaComponentsForEnv(envId, tenantId)).thenReturn(true);
    ApiResponse response =
        service.deleteEnvironment(envId, KafkaClustersType.SCHEMA_REGISTRY.value);

    assertThat(response.getMessage()).contains(ENV_CLUSTER_TNT_ERR_107);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvironmentWithAssociatedEnv() throws KlawException {
    int tenantId = 101;
    String envId = "20";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.existsKafkaComponentsForEnv(envId, tenantId)).thenReturn(false);
    Env env = buildEnv(envId, tenantId, "Env Name", KafkaClustersType.KAFKA, 1);
    Env associatedEnv = buildEnv("21", tenantId, "Associated Env Name", KafkaClustersType.KAFKA, 2);
    EnvTag associatedEnvTag = new EnvTag();
    associatedEnvTag.setId(associatedEnv.getId());
    env.setAssociatedEnv(associatedEnvTag);
    when(handleDbRequestsJdbc.getEnvDetails(envId, tenantId)).thenReturn(env);
    when(handleDbRequestsJdbc.getEnvDetails(associatedEnv.getId(), tenantId))
        .thenReturn(associatedEnv);
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(envId, tenantId))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse response = service.deleteEnvironment(envId, KafkaClustersType.KAFKA.value);

    assertThat(response.getMessage()).contains(ApiResultStatus.SUCCESS.value);
    assertThat(response.isSuccess()).isTrue();
    verify(manageDatabase, times(1)).removeEnvFromCache(tenantId, 20, false);
    verify(handleDbRequestsJdbc, times(1)).addNewEnv(associatedEnv);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvironmentFailed() throws KlawException {
    int tenantId = 101;
    String envId = "20";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.existsKafkaComponentsForEnv(envId, tenantId)).thenReturn(false);
    Env env = buildEnv(envId, tenantId, "Env Name", KafkaClustersType.KAFKA, 1);
    when(handleDbRequestsJdbc.getEnvDetails(envId, tenantId)).thenReturn(env);
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(envId, tenantId))
        .thenReturn("Failed to delete");
    ApiResponse response = service.deleteEnvironment(envId, KafkaClustersType.KAFKA.value);

    assertThat(response.getMessage()).contains("Failed to delete");
    assertThat(response.isSuccess()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewClusterUnauthorized() {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse response = service.addNewCluster(new KwClustersModel());

    assertThat(response).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewCluster() {
    int tenantId = 101;
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.addNewCluster(any(KwClusters.class)))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    KwClustersModel model = new KwClustersModel();
    model.setClusterName("new cluster");
    model.setClusterType(KafkaClustersType.KAFKA);
    model.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA);
    model.setClusterType(KafkaClustersType.KAFKA);
    ApiResponse response = service.addNewCluster(model);

    assertThat(response).isEqualTo(ApiResponse.SUCCESS);
    verify(commonUtilsService, times(1))
        .updateMetadata(tenantId, EntityType.CLUSTER, MetadataOperationType.CREATE, null);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewClusterFailed() {
    int tenantId = 101;
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.addNewCluster(any(KwClusters.class)))
        .thenReturn("failed to add cluster");

    KwClustersModel model = new KwClustersModel();
    model.setClusterName("new cluster");
    model.setClusterType(KafkaClustersType.KAFKA);
    model.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA);
    model.setClusterType(KafkaClustersType.KAFKA);
    ApiResponse response = service.addNewCluster(model);

    assertThat(response).isEqualTo(ApiResponse.FAILURE);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewClusterNameExists() {
    int tenantId = 101;
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(manageDatabase.getClusters(KafkaClustersType.ALL, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 3));

    KwClustersModel model = new KwClustersModel();
    model.setClusterName("3");
    model.setClusterType(KafkaClustersType.KAFKA);
    ApiResponse response = service.addNewCluster(model);

    assertThat(response.getMessage()).isEqualTo(ENV_CLUSTER_TNT_ERR_102);
    assertThat(response.isSuccess()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getSyncEnvsEmptySyncCluster() {
    when(mailService.getUserName(anyString())).thenReturn("user name");
    UserInfo userInfo = buildUserInfo();
    when(handleDbRequestsJdbc.getUsersInfo("user name")).thenReturn(userInfo);
    KwTenantConfigModel model = new KwTenantConfigModel();
    when(manageDatabase.getTenantConfig())
        .thenReturn(
            new HashMap<>() {
              {
                put(101, model);
              }
            });
    List<EnvIdInfo> result = service.getSyncEnvs();

    assertThat(result).isEmpty();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getSyncEnvsFailedToGetSyncCluster() {
    when(mailService.getUserName(anyString())).thenReturn("user name");
    UserInfo userInfo = buildUserInfo();
    when(handleDbRequestsJdbc.getUsersInfo("user name")).thenReturn(userInfo);
    when(manageDatabase.getTenantConfig()).thenReturn(null);
    List<EnvIdInfo> result = service.getSyncEnvs();

    assertThat(result).isEmpty();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getSyncEnvs() {
    when(mailService.getUserName(anyString())).thenReturn("user name");
    UserInfo userInfo = buildUserInfo();
    when(handleDbRequestsJdbc.getUsersInfo("user name")).thenReturn(userInfo);
    KwTenantConfigModel model = new KwTenantConfigModel();
    model.setBaseSyncEnvironment("Sync Cluster");
    when(manageDatabase.getTenantConfig())
        .thenReturn(
            new HashMap<>() {
              {
                put(101, model);
              }
            });
    when(commonUtilsService.getEnvProperty(101, ORDER_OF_TOPIC_ENVS)).thenReturn("Order of Envs");
    when(manageDatabase.getKafkaEnvList(101))
        .thenReturn(
            List.of(
                buildEnv("1", 101, "env1", KafkaClustersType.KAFKA, 1),
                buildEnv("2", 101, "env2", KafkaClustersType.KAFKA, 2)));
    when(manageDatabase.getClusters(KafkaClustersType.KAFKA, 101))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 3));
    List<EnvIdInfo> result = service.getSyncEnvs();

    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getEnvDetailsUnauthorized() {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    when(commonUtilsService.getEnvsFromUserId(anyString())).thenReturn(new HashSet<>());
    EnvModelResponse result = service.getEnvDetails("env id", "cluster type");
    assertThat(result).isNull();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getEnvDetails() {
    int tenantId = 101;
    String envId = "1";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    when(handleDbRequestsJdbc.getEnvDetails(envId, tenantId))
        .thenReturn(buildEnv(envId, tenantId, "env name", KafkaClustersType.KAFKA, 1));
    when(manageDatabase.getClusters(KafkaClustersType.KAFKA, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 3));
    when(manageDatabase.getTenantMap())
        .thenReturn(
            new HashMap<Integer, String>() {
              {
                put(tenantId, "first");
                put(103, "second");
              }
            });
    EnvModelResponse result = service.getEnvDetails(envId, KafkaClustersType.KAFKA.value);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(envId);
    assertThat(result.getName()).isEqualTo("env name");
    assertThat(result.getTenantId()).isEqualTo(tenantId);
    assertThat(result.getClusterName()).isEqualTo("1");
    assertThat(result.getTenantName()).isEqualTo("first");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getClusters() {
    int tenantId = 101;

    when(manageDatabase.getClusters(KafkaClustersType.KAFKA, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 3));
    when(manageDatabase.getAllEnvList(tenantId))
        .thenReturn(
            List.of(
                buildEnv("1", tenantId, "env1", KafkaClustersType.KAFKA, 1),
                buildEnv("2", tenantId, "env2", KafkaClustersType.KAFKA, 2)));
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);

    List<KwClustersModelResponse> result = service.getClusters(KafkaClustersType.KAFKA.value);
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(0).getClusterId()).isEqualTo(1);
    assertThat(result.get(0).isShowDeleteCluster()).isFalse();
    assertThat(result.get(1).getClusterId()).isEqualTo(2);
    assertThat(result.get(1).isShowDeleteCluster()).isFalse();
    assertThat(result.get(2).getClusterId()).isEqualTo(3);
    assertThat(result.get(2).isShowDeleteCluster()).isTrue();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getMyTenantInfo() {
    int tenantId = 101;

    KwTenants tenant = buildTenants(tenantId);
    when(handleDbRequestsJdbc.getMyTenants(tenantId)).thenReturn(Optional.of(tenant));
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    KwTenantModel result = service.getMyTenantInfo();
    assertThat(result.getTenantName()).isEqualTo(tenant.getTenantName());
    assertThat(result.getContactPerson()).isEqualTo(tenant.getContactPerson());
    assertThat(result.isActiveTenant()).isEqualTo(Boolean.valueOf(tenant.getIsActive()));
    assertThat(result.getOrgName()).isEqualTo(tenant.getOrgName());
    assertThat(result.getTenantDesc()).isEqualTo(tenant.getTenantDesc());
    assertThat(result.getTenantId()).isEqualTo(tenant.getTenantId());
    assertThat(result.isAuthorizedToDelete()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getAllTenantsNotSuperAdmin() {
    when(handleDbRequestsJdbc.getUsersInfo(anyString())).thenReturn(buildUserInfo());
    List<KwTenantModel> result = service.getAllTenants();
    assertThat(result).isEmpty();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getAllTenants() {
    UserInfo info1 = buildUserInfo();
    info1.setMailid("Mail Id 1");
    info1.setRole(RolesType.SUPERADMIN.name());
    when(handleDbRequestsJdbc.getUsersInfo(anyString())).thenReturn(info1);
    KwTenants tenants1 = buildTenants(101);
    tenants1.setTenantName("tenant 101");
    KwTenants tenants2 = buildTenants(102);
    tenants2.setTenantName("tenant 102");
    when(handleDbRequestsJdbc.getTenants()).thenReturn(List.of(tenants1, tenants2));
    UserInfo info2 = buildUserInfo();
    info2.setMailid("Mail Id 2");
    info2.setTenantId(102);
    info2.setRole(RolesType.SUPERADMIN.name());
    when(manageDatabase.getUserInfoMap(RolesType.SUPERADMIN))
        .thenReturn(
            new HashMap<>() {
              {
                put(101, info1);
                put(102, info2);
              }
            });
    List<KwTenantModel> result = service.getAllTenants();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getTenantName()).isEqualTo(tenants1.getTenantName());
    assertThat(result.get(1).getTenantName()).isEqualTo(tenants2.getTenantName());
    assertThat(result.get(0).getTenantId()).isEqualTo(tenants1.getTenantId());
    assertThat(result.get(1).getTenantId()).isEqualTo(tenants2.getTenantId());
    assertThat(result.get(0).getEmailId()).isEqualTo(info1.getMailid());
    assertThat(result.get(1).getEmailId()).isEqualTo(info2.getMailid());
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getEnvsForRequestTopicsCluster() {
    int tenantId = 101;
    when(handleDbRequestsJdbc.getUsersInfo(anyString())).thenReturn(buildUserInfo());
    when(commonUtilsService.getEnvProperty(tenantId, REQUEST_TOPICS_OF_ENVS)).thenReturn("1,2,3");
    when(commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS)).thenReturn("2,1,3");
    when(manageDatabase.getKafkaEnvList(tenantId))
        .thenReturn(
            List.of(
                buildEnv("1", tenantId, "env1", KafkaClustersType.KAFKA, 1),
                buildEnv("2", tenantId, "env2", KafkaClustersType.KAFKA, 2),
                buildEnv("3", tenantId, "env3", KafkaClustersType.KAFKA, 3)));
    when(manageDatabase.getClusters(KafkaClustersType.KAFKA, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 3));
    List<EnvModelResponse> result = service.getEnvsForRequestTopicsCluster();
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(0).getId()).isEqualTo("2");
    assertThat(result.get(1).getId()).isEqualTo("1");
    assertThat(result.get(2).getId()).isEqualTo("3");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getClusterDetails() {
    int tenantId = 101;
    KwClusters kwClusters = buildClusters(KafkaClustersType.KAFKA, 1).get(1);
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(handleDbRequestsJdbc.getClusterDetails(1, tenantId)).thenReturn(kwClusters);
    KwClustersModelResponse result = service.getClusterDetails("1");
    assertThat(result.getClusterId()).isEqualTo(1);
    assertThat(result.getClusterName()).isEqualTo("1");
    assertThat(result.getClusterType()).isEqualTo(KafkaClustersType.KAFKA);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void updateTenantUnauthorized() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse result = service.updateTenant(new KwTenantModel());
    assertThat(result).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void updateTenantFailed() throws KlawException {
    int tenantId = 101;
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequestsJdbc.addNewTenant(any(KwTenants.class)))
        .thenReturn("failed to add new tenant");
    ApiResponse result = service.updateTenant(new KwTenantModel());
    assertThat(result.getMessage()).isEqualTo("failed to add new tenant");
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void updateTenant() throws KlawException {
    int tenantId = 101;
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequestsJdbc.addNewTenant(any(KwTenants.class)))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse result = service.updateTenant(new KwTenantModel());
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(result.isSuccess()).isTrue();
    verify(commonUtilsService, times(1))
        .updateMetadata(tenantId, EntityType.TENANT, MetadataOperationType.UPDATE, null);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getClusterInfoFromEnvUnauthorized() {
    int tenantId = 101;
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    when(commonUtilsService.getEnvsFromUserId(anyString())).thenReturn(new HashSet<>());
    ClusterInfo result = service.getClusterInfoFromEnv("1", KafkaClustersType.KAFKA.value);
    assertThat(result).isNull();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getClusterInfoFromEnv() {
    int tenantId = 101;
    String envId = "1";
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequestsJdbc.getEnvDetails(envId, tenantId))
        .thenReturn(buildEnv(envId, tenantId, "env1", KafkaClustersType.KAFKA, 1));
    when(handleDbRequestsJdbc.getClusterDetails(1, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 1).get(1));
    ClusterInfo result = service.getClusterInfoFromEnv(envId, KafkaClustersType.KAFKA.value);
    assertThat(result.isAivenCluster()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteClusterUnauthorized() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse result = service.deleteCluster("cluster id");
    assertThat(result).isEqualTo(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteClusterNotAllowed() throws KlawException {
    int tenantId = 101;
    String clusterId = "1";
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(manageDatabase.getAllEnvList(tenantId))
        .thenReturn(List.of(buildEnv("1", tenantId, "env1", KafkaClustersType.KAFKA, 1)));
    ApiResponse result = service.deleteCluster(clusterId);
    assertThat(result.getMessage()).isEqualTo(ENV_CLUSTER_TNT_ERR_104);
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteClusterFailed() throws KlawException {
    int tenantId = 101;
    String clusterId = "2";
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(manageDatabase.getAllEnvList(tenantId))
        .thenReturn(List.of(buildEnv("1", tenantId, "env1", KafkaClustersType.KAFKA, 1)));
    when(handleDbRequestsJdbc.deleteCluster(2, tenantId)).thenReturn("Failed to delete cluster");
    ApiResponse result = service.deleteCluster(clusterId);
    assertThat(result.getMessage()).isEqualTo("Failed to delete cluster");
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteCluster() throws KlawException {
    int tenantId = 101;
    String clusterId = "2";
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(manageDatabase.getAllEnvList(tenantId))
        .thenReturn(List.of(buildEnv("1", tenantId, "env1", KafkaClustersType.KAFKA, 1)));
    when(handleDbRequestsJdbc.deleteCluster(2, tenantId)).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse result = service.deleteCluster(clusterId);
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getConnectorEnvs() {
    int tenantId = 101;
    when(commonUtilsService.getEnvProperty(tenantId, ORDER_OF_KAFKA_CONNECT_ENVS))
        .thenReturn("3,2");
    when(manageDatabase.getKafkaConnectEnvList(tenantId))
        .thenReturn(
            List.of(
                buildEnv("2", tenantId, "env2", KafkaClustersType.KAFKA_CONNECT, 2),
                buildEnv("3", tenantId, "env3", KafkaClustersType.KAFKA_CONNECT, 3)));
    when(manageDatabase.getClusters(KafkaClustersType.KAFKA_CONNECT, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA_CONNECT, 3));
    when(handleDbRequestsJdbc.getMyTenants(tenantId))
        .thenReturn(Optional.of(buildTenants(tenantId)));
    List<EnvModelResponse> result = service.getConnectorEnvs();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo("3");
    assertThat(result.get(1).getId()).isEqualTo("2");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getClustersPaginated() {
    int tenantId = 101;
    when(manageDatabase.getClusters(KafkaClustersType.KAFKA, tenantId))
        .thenReturn(buildClusters(KafkaClustersType.KAFKA, 3));

    List<KwClustersModelResponse> result =
        service.getClustersPaginated(KafkaClustersType.KAFKA, "1", "1", "1");
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getClusterId()).isEqualTo(1);
  }

  private KwTenants buildTenants(int tenantId) {
    KwTenants tenant = new KwTenants();
    tenant.setTenantName("tenant name");
    tenant.setContactPerson("contact person");
    tenant.setOrgName("org name");
    tenant.setTenantDesc("tenant desc");
    tenant.setTenantId(tenantId);
    tenant.setIsActive("true");

    return tenant;
  }

  private static Stream<Arguments> getEnvs() {
    return Stream.of(
        Arguments.arguments(KafkaClustersType.KAFKA, "", "1", 3),
        Arguments.arguments(KafkaClustersType.SCHEMA_REGISTRY, "", "1", 3),
        Arguments.arguments(KafkaClustersType.KAFKA_CONNECT, "", "1", 3),
        Arguments.arguments(KafkaClustersType.KAFKA_CONNECT, "DEV", "1", 1),
        Arguments.arguments(KafkaClustersType.KAFKA, "3", "1", 1),
        Arguments.arguments(KafkaClustersType.SCHEMA_REGISTRY, "PREPROD", "1", 0),
        Arguments.arguments(KafkaClustersType.SCHEMA_REGISTRY, "", "2", 0));
  }

  private static Env generateKafkaEnv(String id, String Kafka) {
    Env kafkaEnv = new Env();
    kafkaEnv.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    kafkaEnv.setId(id);
    kafkaEnv.setTenantId(101);
    kafkaEnv.setName(Kafka);
    return kafkaEnv;
  }

  private static EnvModel getTestEnvModel(EnvTag envTag) {
    EnvModel env = new EnvModel();
    env.setName("DEV");
    env.setType(KafkaClustersType.KAFKA.value);
    env.setTenantId(101);
    env.setAssociatedEnv(envTag);
    EnvParams params = new EnvParams();
    params.setMaxPartitions("3");
    params.setDefaultPartitions("1");
    params.setDefaultRepFactor("1");
    params.setMaxRepFactor("2");
    env.setParams(params);
    return env;
  }

  private static EnvModel getTestSchemaEnvModel(EnvTag envTag) {
    EnvModel env = new EnvModel();
    env.setName("DEV_SCH");
    env.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    env.setTenantId(101);
    env.setAssociatedEnv(envTag);
    return env;
  }

  private EnvID buildEnvID(String id, int tenantId) {
    EnvID env = new EnvID();
    env.setId(id);
    env.setTenantId(tenantId);
    return env;
  }

  private UserInfo buildUserInfo() {
    UserInfo info = new UserInfo();
    info.setTenantId(101);
    info.setRole("User");
    info.setTeamId(3);
    return info;
  }

  private Env buildEnv(
      String id, int tenantId, String name, KafkaClustersType type, int clusterId) {
    Env mapping = new Env();
    mapping.setId(id);
    mapping.setName(name);
    mapping.setTenantId(tenantId);
    mapping.setType(type.value);
    mapping.setEnvExists("true");
    mapping.setClusterId(clusterId);
    mapping.setOtherParams("emptyIsh");
    return mapping;
  }

  private static Map<Integer, KwClusters> buildClusters(
      KafkaClustersType type, int numberOfClusters) {
    Map<Integer, KwClusters> map = new HashMap<>();
    for (int i = 1; i <= numberOfClusters; i++) {
      KwClusters cluster = new KwClusters();
      cluster.setClusterName(Integer.toString(i));
      cluster.setServiceName(Integer.toString(i));
      cluster.setProjectName(Integer.toString(i));
      cluster.setClusterId(i);
      cluster.setTenantId(101);
      cluster.setClusterType(type.value);
      cluster.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA.value);
      cluster.setProtocol(KafkaSupportedProtocol.SSL);
      map.put(i, cluster);
    }
    return map;
  }
}
