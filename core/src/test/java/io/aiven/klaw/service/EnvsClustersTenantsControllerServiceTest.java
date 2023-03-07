package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvID;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawValidationException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
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
    EnvModel env = getTestEnvModel();
    Env SchemaEnv = new Env();
    SchemaEnv.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    SchemaEnv.setId("9");
    SchemaEnv.setName("Schema");
    when(handleDbRequestsJdbc.addNewEnv(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequestsJdbc.selectEnvDetails(anyString(), anyInt())).thenReturn(SchemaEnv);
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getResult()).contains("success");
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"ADMIN", "USER"})
  void addNewEnvNameAlreadyInUse() throws KlawException, KlawValidationException {
    EnvModel env = getTestEnvModel();
    when(handleDbRequestsJdbc.selectAllEnvs(anyInt()))
        .thenReturn(
            List.of(
                buildEnv("4", 101, "DEV", KafkaClustersType.KAFKA),
                buildEnv("5", 101, "TST", KafkaClustersType.SCHEMA_REGISTRY)));
    when(manageDatabase.getKafkaEnvList(anyInt()))
        .thenReturn(List.of(buildEnv("4", 101, "DEV", KafkaClustersType.KAFKA)));
    ApiResponse response = service.addNewEnv(env);
    assertThat(response.getResult())
        .contains("Failure. Please choose a different name. This environment name already exists.");
  }

  private static EnvModel getTestEnvModel() {
    EnvModel env = new EnvModel();
    env.setName("DEV");
    env.setType(KafkaClustersType.KAFKA.value);
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

  private Env buildEnv(String id, int tenantId, String name, KafkaClustersType type) {
    Env mapping = new Env();
    mapping.setId(id);
    mapping.setName(name);
    mapping.setTenantId(tenantId);
    mapping.setType(type.value);
    mapping.setEnvExists("true");
    return mapping;
  }
}
