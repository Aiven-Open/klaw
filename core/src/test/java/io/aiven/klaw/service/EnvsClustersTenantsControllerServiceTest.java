package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvID;
import io.aiven.klaw.dao.EnvMapping;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EnvType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Captor ArgumentCaptor<EnvMapping> mappingCapture;

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
  void getEnvMapping() {
    when(handleDbRequestsJdbc.findEnvMappingById(any()))
        .thenReturn(buildEnvMapping("2", 101, "DEV"));
    EnvMapping mapping = service.getEnvMapping("2");
    assertThat(mapping.getId()).isEqualTo("2");
    assertThat(mapping.getName()).isEqualTo("DEV");
  }

  @Test
  @WithMockUser(
      username = "chris",
      authorities = {"ADMIN", "USER"})
  void getEnvMappingWhereMappingDoesNotExist() {
    when(handleDbRequestsJdbc.findEnvMappingById(any())).thenReturn(null);
    EnvMapping mapping = service.getEnvMapping("2");
    assertThat(mapping).isNull();
  }

  @Test
  @WithMockUser(
      username = "chris",
      authorities = {"ADMIN", "USER"})
  void testGetEnvMapping() {
    when(handleDbRequestsJdbc.getAllEnvMappingsForTenant(anyInt()))
        .thenReturn(List.of(buildEnvMapping("2", 101, "TST"), buildEnvMapping("1", 101, "DEV")));
    List<EnvMapping> mappings = service.getAllEnvMappings();
    assertThat(mappings.size()).isEqualTo(2);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void testGetEnvMappingWhereNoneExist() {
    when(handleDbRequestsJdbc.getAllEnvMappingsForTenant(anyInt())).thenReturn(null);
    List<EnvMapping> mappings = service.getAllEnvMappings();
    assertThat(mappings).isNull();
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnv() throws KlawException {
    EnvModel env = getTestEnvModel();
    when(handleDbRequestsJdbc.addNewEnv(any(), any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getResult()).contains("success");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnvNameAlreadyInUse() throws KlawException {
    EnvModel env = getTestEnvModel();
    when(handleDbRequestsJdbc.selectAllEnvs(anyInt()))
        .thenReturn(
            List.of(
                buildEnv("4", 101, "DEV", EnvType.KAFKA),
                buildEnv("5", 101, "TST", EnvType.SCHEMAREGISTRY)));
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(List.of(buildEnv("4", 101, "DEV", EnvType.KAFKA)));
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getResult())
        .contains("Failure. Please choose a different name. This environment name already exists.");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnvNameIncrementID() throws KlawException {
    EnvModel env = getTestEnvModel();
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(
            List.of(
                buildEnv("4", 101, "DEV", EnvType.KAFKA),
                buildEnv("5", 101, "TST", EnvType.KAFKA)));
    when(handleDbRequestsJdbc.addNewEnv(any(), any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(1))
        .addNewEnv(envCapture.capture(), mappingCapture.capture());

    // Should be incrementer by 1 from the id 5 given to the mocked TST env
    assertThat(envCapture.getValue().getId()).isEqualTo("6");
    // Its a kafka type so it should also create its own Mapping Env
    assertThat(mappingCapture.getValue().getId()).isEqualTo("6");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnvNameThatExistsInDeletedState() throws KlawException {
    EnvModel env = getTestEnvModel();
    Env deletedEnv = buildEnv("4", 101, "DEV", EnvType.KAFKA);
    deletedEnv.setEnvExists("false");
    when(handleDbRequestsJdbc.addNewEnv(any(), any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.selectAllEnvs(anyInt())).thenReturn(List.of(deletedEnv));
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(List.of(deletedEnv, buildEnv("5", 101, "TST", EnvType.KAFKA)));
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getResult()).contains("success");

    verify(handleDbRequestsJdbc, times(1))
        .addNewEnv(envCapture.capture(), mappingCapture.capture());

    // Should reuse the existing deleted env id
    assertThat(envCapture.getValue().getId()).isEqualTo("4");
    // Its a kafka type so it should also create its own Mapping Env
    assertThat(mappingCapture.getValue().getId()).isEqualTo("4");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvNameThatIsAlreadyInDeletedState() throws KlawException {

    Env deletedEnv = buildEnv("4", 101, "DEV", EnvType.KAFKA);
    deletedEnv.setEnvExists("false");
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.envMappingExists(any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(List.of(deletedEnv, buildEnv("5", 101, "TST", EnvType.KAFKA)));

    ApiResponse response = service.deleteEnvironment("4", EnvType.KAFKA.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(0)).findEnvMappingById(any());
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvNameWithAssociatedEnvMappingNoLinks() throws KlawException {

    Env deletedEnv = buildEnv("4", 101, "DEV", EnvType.KAFKA);
    deletedEnv.setEnvExists("false");
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.envMappingExists(any())).thenReturn(true);
    when(handleDbRequestsJdbc.findEnvMappingById(any()))
        .thenReturn(buildEnvMapping("4", 101, "DEV"));
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(List.of(deletedEnv, buildEnv("5", 101, "TST", EnvType.KAFKA)));

    ApiResponse response = service.deleteEnvironment("4", EnvType.KAFKA.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(1)).findEnvMappingById(any());
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteEnvNameWithAssociatedEnvMappingWithMultipleLinks() throws KlawException {

    Env schemaEnv = buildEnv("5", 101, "DEV", EnvType.SCHEMAREGISTRY);
    schemaEnv.setAssociatedEnv("4");
    Env connectorEnv = buildEnv("6", 101, "DEV", EnvType.SCHEMAREGISTRY);
    connectorEnv.setAssociatedEnv("4");
    EnvMapping mapping = buildEnvMapping("4", 101, "DEV");
    mapping.setSchemaEnvs(List.of(new EnvTag("5", "DEV_SCH")));
    mapping.setConnectorEnvs(List.of(new EnvTag("6", "DEV_CONN")));
    // setup complete
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.envMappingExists(any())).thenReturn(true);
    when(handleDbRequestsJdbc.findEnvMappingById(any())).thenReturn(mapping);
    when(handleDbRequestsJdbc.selectEnvDetails(eq("5"), eq(101))).thenReturn(schemaEnv);
    when(handleDbRequestsJdbc.selectEnvDetails(eq("6"), eq(101))).thenReturn(connectorEnv);
    // mocking complete
    ApiResponse response = service.deleteEnvironment("4", EnvType.KAFKA.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(1)).findEnvMappingById(any());
    // Twice once for each env.
    verify(handleDbRequestsJdbc, times(2)).addNewEnv(any(), eq(null));
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteSchemaEnvNoAssociatedEnvs() throws KlawException {

    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.selectEnvDetails(any(), anyInt()))
        .thenReturn(buildEnv("4", 101, "DEV", EnvType.SCHEMAREGISTRY));

    ApiResponse response = service.deleteEnvironment("4", EnvType.SCHEMAREGISTRY.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(0)).findEnvMappingById(any());
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteConnectorEnvNoAssociatedEnvs() throws KlawException {

    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.selectEnvDetails(any(), anyInt()))
        .thenReturn(buildEnv("4", 101, "DEV", EnvType.KAFKACONNECT));

    ApiResponse response = service.deleteEnvironment("4", EnvType.KAFKACONNECT.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(0)).findEnvMappingById(any());
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteSchemaEnvWithAssociatedEnvs() throws KlawException {
    Env schemaEnv = buildEnv("5", 101, "DEV_SCH", EnvType.SCHEMAREGISTRY);
    schemaEnv.setAssociatedEnv("4");
    EnvMapping mapping = buildEnvMapping("4", 101, "DEV");
    mapping.setSchemaEnvs(List.of(new EnvTag("5", "DEV_SCH")));
    mapping.setConnectorEnvs(List.of(new EnvTag("6", "DEV_CONN")));

    when(handleDbRequestsJdbc.selectEnvDetails(any(), anyInt())).thenReturn(schemaEnv);
    when(handleDbRequestsJdbc.findEnvMappingById(any())).thenReturn(mapping);
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse response = service.deleteEnvironment("5", EnvType.SCHEMAREGISTRY.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(1)).findEnvMappingById(any());
    verify(handleDbRequestsJdbc, times(1)).updateEnvMapping(mappingCapture.capture());
    assertThat(mappingCapture.getValue().getSchemaEnvs().size()).isEqualTo(0);
    assertThat(mappingCapture.getValue().getConnectorEnvs().size()).isEqualTo(1);
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void deleteConnectorEnvWithAssociatedEnvs() throws KlawException {
    Env connEnv = buildEnv("6", 101, "DEV_CONN", EnvType.KAFKACONNECT);
    connEnv.setAssociatedEnv("4");
    EnvMapping mapping = buildEnvMapping("4", 101, "DEV");
    mapping.setSchemaEnvs(List.of(new EnvTag("5", "DEV_SCH")));
    mapping.setConnectorEnvs(List.of(new EnvTag("6", "DEV_CONN")));

    when(handleDbRequestsJdbc.selectEnvDetails(any(), anyInt())).thenReturn(connEnv);
    when(handleDbRequestsJdbc.findEnvMappingById(any())).thenReturn(mapping);
    when(handleDbRequestsJdbc.deleteEnvironmentRequest(any(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse response = service.deleteEnvironment("6", EnvType.KAFKACONNECT.value);

    assertThat(response.getResult()).contains("success");
    verify(handleDbRequestsJdbc, times(1)).findEnvMappingById(any());
    verify(handleDbRequestsJdbc, times(1)).updateEnvMapping(mappingCapture.capture());

    assertThat(mappingCapture.getValue().getConnectorEnvs().size()).isEqualTo(0);
    assertThat(mappingCapture.getValue().getSchemaEnvs().size()).isEqualTo(1);
  }

  private static EnvModel getTestEnvModel() {
    EnvModel env = new EnvModel();
    env.setName("DEV");
    env.setType(EnvType.KAFKA.value);
    env.setTenantId(101);
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

  private EnvMapping buildEnvMapping(String id, int tenantId, String name) {
    EnvMapping mapping = new EnvMapping();
    mapping.setId(id);
    mapping.setName(name);
    mapping.setTenantId(tenantId);
    return mapping;
  }

  private Env buildEnv(String id, int tenantId, String name, EnvType type) {
    Env mapping = new Env();
    mapping.setId(id);
    mapping.setName(name);
    mapping.setTenantId(tenantId);
    mapping.setType(type.value);
    mapping.setEnvExists("true");
    return mapping;
  }
}
