package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
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
import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawValidationException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.response.EnvModelResponse;
import io.aiven.klaw.model.response.EnvParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @ParameterizedTest(name = "{2}: actual={0} / expected={1}")
  @MethodSource
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void getEnvs(KafkaClustersType type, String searchBy,String pageNo, int expectedMatches) throws KlawValidationException, KlawException {

    when(commonUtilsService.getEnvProperty(eq(101), eq(ORDER_OF_TOPIC_ENVS))).thenReturn("1,2,3");
    //dependent on the type
    when(manageDatabase.getKafkaEnvList(eq(101))).thenReturn(List.of(buildEnv("1",101,"DEV",type,1),buildEnv("2",101,"TST",type,2),buildEnv("3",101,"PRD",type,3)));
    when(manageDatabase.getSchemaRegEnvList(eq(101))).thenReturn(List.of(buildEnv("1",101,"DEV",type,1),buildEnv("2",101,"TST",type,2),buildEnv("3",101,"PRD",type,3)));
    when(manageDatabase.getKafkaConnectEnvList(eq(101))).thenReturn(List.of(buildEnv("1",101,"DEV",type,1),buildEnv("2",101,"TST",type,2),buildEnv("3",101,"PRD",type,3)));

    when(manageDatabase.getClusters(eq(type),eq(101))).thenReturn(buildClusters(type,3));
    when(manageDatabase
             .getTenantMap()).thenReturn(new HashMap<>(){{put(101,"");}});
    when(commonUtilsService.isNotAuthorizedUser(any(),any())).thenReturn(false);

    List<EnvModelResponse> response = service.getEnvsPaginated(type, "", pageNo, searchBy);
    
    assertThat(response).hasSize(expectedMatches);
  }

  private static Stream<Arguments> getEnvs() {
    return Stream.of(Arguments.arguments(KafkaClustersType.KAFKA,"","1",3),
                     Arguments.arguments(KafkaClustersType.SCHEMA_REGISTRY,"","1",3),
                     Arguments.arguments(KafkaClustersType.KAFKA_CONNECT,"","1",3),
                     Arguments.arguments(KafkaClustersType.KAFKA_CONNECT,"DEV","1",1),
                     Arguments.arguments(KafkaClustersType.KAFKA,"3","1",1),
                     Arguments.arguments(KafkaClustersType.SCHEMA_REGISTRY,"PREPROD","1",0),
                     Arguments.arguments(KafkaClustersType.SCHEMA_REGISTRY,"","2",0));
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

  @Test
  void deleteEnvironment() {}

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

  private static Map<Integer,KwClusters> buildClusters(KafkaClustersType type, int numberOfClusters){
    Map<Integer, KwClusters> map = new HashMap<>();
    for(int i=1;i<=numberOfClusters;i++) {
      KwClusters cluster = new KwClusters();
      cluster.setClusterName(Integer.toString(i));
      cluster.setServiceName(Integer.toString(i));
      cluster.setProjectName(Integer.toString(i));
      cluster.setTenantId(101);
      cluster.setClusterType(type.value);
      map.put(i,cluster);
    }
    return map;
  }
}
