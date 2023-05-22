package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.response.SyncSchemasList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
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
public class SchemaRegistrySyncControllerServiceTest {

  public static final String TESTTOPIC = "topic-1";
  @Mock private UserDetails userDetails;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock private MailUtils mailService;

  @Mock private ManageDatabase manageDatabase;

  @Mock private UserInfo userInfo;

  @Mock private ClusterApiService clusterApiService;

  @Mock CommonUtilsService commonUtilsService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  private SchemaRegistrySyncControllerService schemaRegistrySyncControllerService;

  @Mock private Map<Integer, KwClusters> clustersHashMap;
  @Mock private KwClusters kwClusters;

  private ObjectMapper mapper = new ObjectMapper();
  private UtilMethods utilMethods;

  private Env env;

  @Captor private ArgumentCaptor<SchemaRequest> schemaRequestCaptor;

  @BeforeEach
  public void setUp() throws Exception {
    this.env = new Env();
    env.setId("1");
    env.setName("DEV");
    utilMethods = new UtilMethods();

    schemaRegistrySyncControllerService =
        new SchemaRegistrySyncControllerService(clusterApiService, mailService);
    ReflectionTestUtils.setField(
        schemaRegistrySyncControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(schemaRegistrySyncControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        schemaRegistrySyncControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        schemaRegistrySyncControllerService,
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
  public void getSchemasOfEnvironment() throws Exception {
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");
    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    List<Topic> topics = utilMethods.generateTopics(14);

    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(clusterApiService.getSchemasFromCluster(anyString(), any(), anyString(), anyInt()))
        .thenReturn(utilMethods.getSchemasInfoOfEnv());
    when(commonUtilsService.deriveCurrentPage("1", "", 1)).thenReturn("1");
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");
    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment("1", "1", "");
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(userInfo.getRole()).thenReturn("USER");
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
