package io.aiven.klaw.service;

import static io.aiven.klaw.service.SchemaRegistrySyncControllerService.IN_SYNC;
import static io.aiven.klaw.service.SchemaRegistrySyncControllerService.NOT_IN_SYNC;
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
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncSchemaUpdates;
import io.aiven.klaw.model.response.SchemaDetailsResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  public void getSchemasOfEnvironmentNotInSync() throws Exception {
    stubUserInfo();

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    List<Topic> topics = utilMethods.generateTopics(14);
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");
    when(commonUtilsService.deriveCurrentPage("1", "", 1)).thenReturn("1");
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);

    when(clusterApiService.getSchemasFromCluster(anyString(), any(), anyString(), anyInt()))
        .thenReturn(utilMethods.getSchemasInfoOfEnv());
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);

    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment("1", "1", "", "", true);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(0).getRemarks())
        .isEqualTo(NOT_IN_SYNC);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(1).getRemarks())
        .isEqualTo(NOT_IN_SYNC);
  }

  @Test
  public void getSchemasOfEnvironmentInSyncAndNotInSync() throws Exception {
    stubUserInfo();

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    List<Topic> topics = utilMethods.generateTopics(14);
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");
    when(commonUtilsService.deriveCurrentPage("1", "", 1)).thenReturn("1");
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);

    when(clusterApiService.getSchemasFromCluster(anyString(), any(), anyString(), anyInt()))
        .thenReturn(utilMethods.getSchemasInfoOfEnv());
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);

    Map<String, Set<String>> topicSchemaVersionsInDb = utilMethods.getTopicSchemaVersionsInDb();
    when(handleDbRequests.getTopicAndVersionsForEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(topicSchemaVersionsInDb);

    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment("1", "1", "", "", true);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(0).getRemarks())
        .isEqualTo(IN_SYNC);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(1).getRemarks())
        .isEqualTo(IN_SYNC);

    // All schemas don't exist
    topicSchemaVersionsInDb.put("Topic0", Set.of("1")); // remove a version from db
    topicSchemaVersionsInDb.put("Topic1", Set.of("1", "2")); // remove a version from db
    when(handleDbRequests.getTopicAndVersionsForEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(topicSchemaVersionsInDb);
    schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment("1", "1", "", "", true);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(0).getRemarks())
        .isEqualTo(NOT_IN_SYNC);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(1).getRemarks())
        .isEqualTo(NOT_IN_SYNC);

    // Only one schema differs, set showAllTopics to false
    topicSchemaVersionsInDb.put("Topic0", Set.of("1")); // remove a version from db, so not-in-sync
    topicSchemaVersionsInDb.put("Topic1", Set.of("1", "2", "3")); // make it in sync with db
    when(handleDbRequests.getTopicAndVersionsForEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(topicSchemaVersionsInDb);
    schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment("1", "1", "", "", false);

    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(1);
  }

  @Test
  public void updateDbFromCluster() throws Exception {
    stubUserInfo();
    String topicName = "2ndTopic";

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    SyncSchemaUpdates syncSchemaUpdates = new SyncSchemaUpdates();
    syncSchemaUpdates.setKafkaEnvSelected("1");
    syncSchemaUpdates.setTopicList(List.of("2ndTopic"));

    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);

    when(clusterApiService.getAvroSchema(anyString(), any(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.createSchemaList());
    List<Topic> topicList = utilMethods.getTopics();
    topicList.get(0).setTopicname(topicName);
    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);

    ApiResponse apiResponse =
        schemaRegistrySyncControllerService.updateDbFromCluster(syncSchemaUpdates);
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage()).isEqualTo("Topics " + syncSchemaUpdates.getTopicList());
  }

  @Test
  public void getSchemaOfTopic() throws Exception {
    int schemaVersion = 1;
    String topicName = "2ndTopic";
    String kafkaEnvId = "1";

    stubUserInfo();

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(clusterApiService.getAvroSchema(anyString(), any(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.createSchemaList());

    SchemaDetailsResponse schemaDetailsResponse =
        schemaRegistrySyncControllerService.getSchemaOfTopicFromCluster(
            topicName, schemaVersion, kafkaEnvId);
    assertThat(schemaDetailsResponse.getSchemaContent()).contains("klaw.avro"); // namespace
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(userInfo.getRole()).thenReturn("USER");
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
