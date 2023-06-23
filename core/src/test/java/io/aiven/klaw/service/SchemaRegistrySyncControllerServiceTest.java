package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.SYNC_102;
import static io.aiven.klaw.error.KlawErrorMessages.SYNC_103;
import static io.aiven.klaw.service.SchemaRegistrySyncControllerService.IN_SYNC;
import static io.aiven.klaw.service.SchemaRegistrySyncControllerService.NOT_IN_SYNC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncSchemaUpdates;
import io.aiven.klaw.model.cluster.SchemasInfoOfClusterResponse;
import io.aiven.klaw.model.response.SchemaDetailsResponse;
import io.aiven.klaw.model.response.SchemaSubjectInfoResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SchemaRegistrySyncControllerServiceTest {
  public static final String TEAM = "Octopus";
  @Mock private UserDetails userDetails;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock private MailUtils mailService;

  @Mock private ManageDatabase manageDatabase;

  @Mock private UserInfo userInfo;

  @Mock private ClusterApiService clusterApiService;

  @Mock CommonUtilsService commonUtilsService;

  private SchemaRegistrySyncControllerService schemaRegistrySyncControllerService;
  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() throws Exception {
    Env env = new Env();
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
  @Order(1)
  public void getSchemasOfEnvironmentNotInSyncSourceCluster() throws Exception {
    stubUserInfo();

    String source = "cluster";
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
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(0).getRemarks())
        .isEqualTo(NOT_IN_SYNC);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(1).getRemarks())
        .isEqualTo(NOT_IN_SYNC);
  }

  @Test
  @Order(2)
  public void getSchemasOfEnvironmentInSyncAndNotInSyncSourceCluster() throws Exception {
    stubUserInfo();

    String source = "cluster";
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
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
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
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
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
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", false, source, 0);

    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(1);
  }

  @Test
  @Order(3)
  public void getSchemasOfEnvironmentSourceMetadata() throws Exception {
    stubUserInfo();

    String source = "metadata";
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    List<Topic> topics = utilMethods.generateTopics(14);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");
    when(commonUtilsService.deriveCurrentPage("1", "", 1)).thenReturn("1");

    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(topics);
    Map<String, Set<String>> topicSchemaVersionsInDb = utilMethods.getTopicSchemaVersionsInDb();
    when(handleDbRequests.getTopicAndVersionsForEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(topicSchemaVersionsInDb);

    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList())
        .extracting(SchemaSubjectInfoResponse::getTopic)
        .containsExactlyInAnyOrder("Topic0", "Topic1");
  }

  @Test
  @Order(4)
  public void getSchemasOfEnvironmentNoSchemasSourceMetadata() throws Exception {
    stubUserInfo();

    String source = "metadata";
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    List<Topic> topics = utilMethods.generateTopics(14);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");
    when(commonUtilsService.deriveCurrentPage("1", "", 0)).thenReturn("1");

    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(topics);
    when(handleDbRequests.getTopicAndVersionsForEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(new HashMap<>());

    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(0);
  }

  @Test
  @Order(5)
  public void updateDbFromCluster() throws Exception {
    stubUserInfo();
    String topicName = "2ndTopic";

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    SyncSchemaUpdates syncSchemaUpdates = new SyncSchemaUpdates();
    syncSchemaUpdates.setSourceKafkaEnvSelected("1");
    syncSchemaUpdates.setTopicList(List.of("2ndTopic"));
    syncSchemaUpdates.setTypeOfSync("SYNC_SCHEMAS");

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
        schemaRegistrySyncControllerService.updateSyncSchemas(syncSchemaUpdates);
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage())
        .isEqualTo(
            "Topics/Schemas "
                + syncSchemaUpdates.getTopicList()
                + "\nSchemas removed "
                + CollectionUtils.emptyIfNull(syncSchemaUpdates.getTopicListForRemoval()));
  }

  @Test
  @Order(6)
  public void updateClusterFromDb() throws Exception {
    stubUserInfo();
    String topicName = "2ndTopic";

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    SyncSchemaUpdates syncSchemaUpdates = new SyncSchemaUpdates();
    syncSchemaUpdates.setSourceKafkaEnvSelected("1");
    syncSchemaUpdates.setTargetKafkaEnvSelected("2");
    syncSchemaUpdates.setTopicList(List.of(topicName));
    syncSchemaUpdates.setTypeOfSync("SYNC_BACK_SCHEMAS");
    syncSchemaUpdates.setTopicsSelectionType("SELECTED_TOPICS");

    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);

    when(clusterApiService.getAvroSchema(anyString(), any(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.createSchemaList());
    ApiResponse apiResponseDelete = ApiResponse.builder().success(true).build();
    ResponseEntity<ApiResponse> responseEntity =
        new ResponseEntity<>(apiResponseDelete, HttpStatus.OK);

    when(clusterApiService.deleteSchema(anyString(), anyString(), anyInt()))
        .thenReturn(responseEntity);
    Map<String, Object> schemaCreationResponseMap = new HashMap<>();
    schemaCreationResponseMap.put("schemaRegistered", Boolean.TRUE);
    schemaCreationResponseMap.put("id", 1234);
    schemaCreationResponseMap.put("compatibility", "BACKWARD");
    schemaCreationResponseMap.put("version", 1);
    ApiResponse apiResponseCreate =
        ApiResponse.builder().success(true).data(schemaCreationResponseMap).build();
    ResponseEntity<ApiResponse> responseEntityCreate =
        new ResponseEntity<>(apiResponseCreate, HttpStatus.OK);

    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(responseEntityCreate);

    List<MessageSchema> schemaList = utilMethods.getMSchemas();
    schemaList.get(0).setTopicname(topicName);
    when(handleDbRequests.getSchemaForTenantAndEnvAndTopic(anyInt(), anyString(), anyString()))
        .thenReturn(schemaList);

    ApiResponse apiResponse =
        schemaRegistrySyncControllerService.updateSyncSchemas(syncSchemaUpdates);
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(((List<String>) apiResponse.getData()))
        .contains("Topics/Schemas result")
        .contains("Schemas deleted for 2ndTopic")
        .contains("Schemas registered on cluster for 2ndTopic Version 1");
  }

  @Test
  @Order(7)
  public void getSchemaOfTopicFromSourceCluster() throws Exception {
    int schemaVersion = 1;
    String topicName = "2ndTopic";
    String kafkaEnvId = "1";
    String source = "cluster";

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
        schemaRegistrySyncControllerService.getSchemaOfTopicFromSource(
            source, topicName, schemaVersion, kafkaEnvId);
    assertThat(schemaDetailsResponse.getSchemaContent()).contains("klaw.avro"); // namespace
  }

  @Test
  @Order(8)
  public void getSchemaOfTopicFromSourceMetadata() throws Exception {
    int schemaVersion = 1;
    String topicName = "2ndTopic";
    String kafkaEnvId = "1";
    String source = "metadata";

    stubUserInfo();

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    List<MessageSchema> schemaList = utilMethods.getMSchemas();
    schemaList.get(0).setTopicname(topicName);
    schemaList.get(0).setSchemafull("\"namespace : klaw.avro\"");
    when(handleDbRequests.getSchemaForTenantAndEnvAndTopicAndVersion(
            anyInt(), anyString(), anyString(), anyString()))
        .thenReturn(schemaList);

    SchemaDetailsResponse schemaDetailsResponse =
        schemaRegistrySyncControllerService.getSchemaOfTopicFromSource(
            source, topicName, schemaVersion, kafkaEnvId);
    assertThat(schemaDetailsResponse.getSchemaContent()).contains("klaw.avro"); // namespace
  }

  @ParameterizedTest
  @MethodSource("generateSchemasToDeleteData")
  @Order(9)
  public void deleteOrphanedSchemaMetaDataFromDb(List<String> schemasToBeDeleted) throws Exception {
    stubUserInfo();
    String topicName = "2ndTopic";

    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    SyncSchemaUpdates syncSchemaUpdates = new SyncSchemaUpdates();
    syncSchemaUpdates.setSourceKafkaEnvSelected("1");
    syncSchemaUpdates.setTopicList(List.of("2ndTopic"));
    syncSchemaUpdates.setTypeOfSync("SYNC_SCHEMAS");
    syncSchemaUpdates.setTopicListForRemoval(schemasToBeDeleted);

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
        schemaRegistrySyncControllerService.updateSyncSchemas(syncSchemaUpdates);
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage())
        .isEqualTo(
            "Topics/Schemas "
                + syncSchemaUpdates.getTopicList()
                + "\nSchemas removed "
                + CollectionUtils.emptyIfNull(syncSchemaUpdates.getTopicListForRemoval()));

    verify(handleDbRequests, times(schemasToBeDeleted.size()))
        .deleteSchema(eq(101), anyString(), eq("1"));
  }

  @Test
  @Order(10)
  public void getSchemaOfTopicFromSourceMetadataWithSchemasToBeDeleted() throws Exception {
    stubUserInfo();

    String source = "cluster";
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");

    List<Topic> topics = utilMethods.generateTopics(1);
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
    when(handleDbRequests.getSchemaForTenantAndEnvAndTopic(eq(101), eq("1"), anyString()))
        .thenReturn(schemaMetaData());
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(3))).thenReturn(TEAM);

    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(0).getRemarks())
        .isEqualTo(NOT_IN_SYNC);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(1).getRemarks())
        .isEqualTo(SYNC_103);
    assertThat(
            schemasInfoOfClusterResponse
                .getSchemaSubjectInfoResponseList()
                .get(1)
                .getPossibleTeams())
        .contains(TEAM, SYNC_102);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().get(1).getTeamname())
        .isEqualTo(TEAM);
  }

  @Test
  @Order(11)
  public void getSchemasOfEnvironmentFromMetadataWithTopicDeletedAndSchemaStillAvailable()
      throws Exception {
    stubUserInfo();

    String source = "cluster";
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());

    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(3))).thenReturn("Team1");
    when(commonUtilsService.deriveCurrentPage("1", "", 1)).thenReturn("1");
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(new ArrayList<>());
    Map<String, Set<String>> topicSchemaVersionsInDb = utilMethods.getTopicSchemaVersionsInDb();

    when(manageDatabase
            .getHandleDbRequests()
            .getSchemaForTenantAndEnvAndTopic(eq(101), eq("1"), anyString()))
        .thenReturn(schemaMetaData());
    when(clusterApiService.getSchemasFromCluster(anyString(), any(), anyString(), anyInt()))
        .thenReturn(utilMethods.getSchemasInfoOfEnv());
    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList())
        .extracting(SchemaSubjectInfoResponse::getTopic)
        .containsExactlyInAnyOrder("Topic0", "Topic1");
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList())
        .extracting(SchemaSubjectInfoResponse::getRemarks)
        .containsExactlyInAnyOrder("ORPHANED", "ORPHANED");
    assertThat(
            schemasInfoOfClusterResponse
                .getSchemaSubjectInfoResponseList()
                .get(0)
                .getPossibleTeams())
        .contains("REMOVE FROM KLAW");

    verify(clusterApiService, times(1))
        .getSchemasFromCluster(anyString(), any(), anyString(), anyInt());
  }

  @Test
  @Order(12)
  public void getSchemasOfEnvironmentFromMetadataWithSchemaDeletedFromCluster() throws Exception {
    stubUserInfo();

    String source = "cluster";
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());
    List<Topic> topics = utilMethods.generateTopics(14);

    SchemasInfoOfClusterResponse clusterResp = new SchemasInfoOfClusterResponse();
    clusterResp.setSchemaInfoOfTopicList(new ArrayList<>());
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(3))).thenReturn("Team1");
    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");
    when(commonUtilsService.deriveCurrentPage("1", "", 1)).thenReturn("1");
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(topics);
    Map<String, Set<String>> topicSchemaVersionsInDb = utilMethods.getTopicSchemaVersionsInDb();
    when(handleDbRequests.getTopicAndVersionsForEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(topicSchemaVersionsInDb);
    when(clusterApiService.getAvroSchema(anyString(), any(), anyString(), anyString(), anyInt()))
        .thenReturn(new TreeMap<>());
    when(manageDatabase
            .getHandleDbRequests()
            .getTeamIdFromSchemaNameAndEnvAndTenantId(anyString(), eq("1"), eq(101)))
        .thenReturn(schemaMetaData().get(0));
    when(clusterApiService.getSchemasFromCluster(anyString(), any(), anyString(), anyInt()))
        .thenReturn(clusterResp);
    SyncSchemasList schemasInfoOfClusterResponse =
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            "1", "1", "", "", true, source, 0);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList())
        .extracting(SchemaSubjectInfoResponse::getTopic)
        .containsExactlyInAnyOrder("Topic0", "Topic1");
    assertThat(schemasInfoOfClusterResponse.getSchemaSubjectInfoResponseList())
        .extracting(SchemaSubjectInfoResponse::getRemarks)
        .containsExactlyInAnyOrder("NOT_ON_CLUSTER", "NOT_ON_CLUSTER");
    assertThat(
            schemasInfoOfClusterResponse
                .getSchemaSubjectInfoResponseList()
                .get(0)
                .getPossibleTeams())
        .contains("REMOVE FROM KLAW");
    verify(clusterApiService, times(1))
        .getSchemasFromCluster(anyString(), any(), anyString(), anyInt());
  }

  @Test
  @Order(13)
  public void getSchemasOfEnvironmentFromMetadataExceptonContactingCluster() throws Exception {
    stubUserInfo();

    String source = "cluster";
    Env env = utilMethods.getEnvLists().get(0);
    env.setAssociatedEnv(new EnvTag("1", "SCH"));
    env.setType("kafka");
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());
    List<Topic> topics = utilMethods.generateTopics(14);

    SchemasInfoOfClusterResponse clusterResp = new SchemasInfoOfClusterResponse();
    clusterResp.setSchemaInfoOfTopicList(new ArrayList<>());
    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    assertThatThrownBy(
            () -> {
              schemaRegistrySyncControllerService.getSchemasOfEnvironment(
                  "1", "1", "", "", true, source, 0);
            })
        .isInstanceOf(KlawException.class);
  }

  private List<MessageSchema> schemaMetaData() {
    List<MessageSchema> schemas = new ArrayList<>();

    MessageSchema schema = new MessageSchema();
    schema.setSchemaId(1);
    schema.setTenantId(101);
    schema.setTopicname("Topic1");
    schema.setTeamId(3);
    schemas.add(schema);

    return schemas;
  }

  private static Stream<Arguments> generateSchemasToDeleteData() {
    return Stream.of(
        Arguments.of(Arrays.asList("schema1", "schema2")),
        Arguments.of(Arrays.asList("schema1")),
        Arguments.of(Arrays.asList("schema1", "schema2", "schema2")),
        Arguments.of(CollectionUtils.emptyCollection()));
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(userInfo.getRole()).thenReturn("USER");
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
