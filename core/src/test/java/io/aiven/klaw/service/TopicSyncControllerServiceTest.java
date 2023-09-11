package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.CRUDResponse;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.SyncBackTopics;
import io.aiven.klaw.model.SyncTopicUpdates;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.model.response.SyncTopicsList;
import io.aiven.klaw.model.response.TopicConfig;
import io.aiven.klaw.model.response.TopicSyncResponseModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.thymeleaf.util.StringUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicSyncControllerServiceTest {

  public static final String SELECTED_TOPICS = "SELECTED_TOPICS";
  public static final String ALL_TOPICS = "ALL_TOPICS";
  public static final int TENANT_ID = 1;
  public static final String USERNAME = "kwusera";
  public static final String TOPIC_NAME_1 = "unittest-01";
  public static final String TOPIC_NAME_2 = "unittest-02";
  @Mock private ClusterApiService clusterApiService;

  @Mock private UserDetails userDetails;

  @Mock private UserInfo userInfo;

  @Mock private Map<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

  @Mock private ManageDatabase manageDatabase;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock CommonUtilsService commonUtilsService;

  @Mock private MailUtils mailService;

  private TopicSyncControllerService topicSyncControllerService;

  @Mock Map<Integer, KwTenantConfigModel> tenantConfig;

  @Mock KwTenantConfigModel tenantConfigModel;

  private UtilMethods utilMethods;

  private Env env = new Env();
  private Env test = new Env();
  private Env uat = new Env();
  private Env prd = new Env();
  @Captor ArgumentCaptor<TopicRequest> topicRequestCaptor;

  @Captor ArgumentCaptor<TopicRequest> updateTopicRequestCaptor;

  @BeforeEach
  public void setUp() throws Exception {
    this.topicSyncControllerService = new TopicSyncControllerService();
    utilMethods = new UtilMethods();
    environmentSetUp();

    ReflectionTestUtils.setField(topicSyncControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(topicSyncControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        topicSyncControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        topicSyncControllerService, "clusterApiService", clusterApiService);

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  private void environmentSetUp() {
    env.setId("1");
    env.setClusterId(1);
    env.setName("DEV");

    test.setId("2");
    test.setName("TST");
    test.setClusterId(2);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(List.of(env, test));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
  }

  private void validatedEnvironmentSetUp(String repFactor, String maxPartitions) {
    env.setId("1");
    env.setClusterId(1);
    env.setName("DEV");
    EnvParams params = new EnvParams();
    params.setTopicPrefix(List.of("Dev-"));
    params.setMaxPartitions(maxPartitions);
    params.setMaxRepFactor(repFactor);
    env.setParams(params);

    test.setId("2");
    test.setName("TST");
    test.setClusterId(2);

    EnvParams tstParams = new EnvParams();
    tstParams.setTopicSuffix(List.of("-TST"));
    tstParams.setMaxPartitions(maxPartitions);
    tstParams.setMaxRepFactor(repFactor);
    test.setParams(tstParams);

    uat.setId("3");
    uat.setName("UAT");
    uat.setClusterId(3);

    EnvParams uatParams = new EnvParams();
    uatParams.setTopicRegex(List.of(".*-UAT-.*"));
    uatParams.setApplyRegex(true);
    uatParams.setMaxPartitions(maxPartitions);
    uatParams.setMaxRepFactor(repFactor);
    uat.setParams(uatParams);

    prd.setId("4");
    prd.setName("PRD");
    prd.setClusterId(4);

    EnvParams prdParams = new EnvParams();
    prdParams.setTopicPrefix(List.of("prd-"));
    prdParams.setTopicSuffix(List.of("-prd"));
    prdParams.setMaxPartitions(maxPartitions);
    prdParams.setMaxRepFactor(repFactor);
    prd.setParams(prdParams);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(List.of(env, test, uat, prd));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
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
  public void updateSyncTopicsSuccess() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.addToSynctopics(any()))
        .thenReturn(
            CRUDResponse.<Topic>builder().resultStatus(ApiResultStatus.SUCCESS.value).build());

    ApiResponse result =
        topicSyncControllerService.updateSyncTopics(utilMethods.getSyncTopicUpdates());
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void updateSyncTopicsNoUpdate() throws KlawException {
    List<SyncTopicUpdates> topicUpdates = new ArrayList<>();

    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getAllEnvIds(anyInt())).thenReturn(Collections.singletonList("1"));

    ApiResponse result = topicSyncControllerService.updateSyncTopics(topicUpdates);
    assertThat(result.getMessage()).isEqualTo("No record updated.");
  }

  @Test
  @Order(3)
  public void updateSyncTopicsNotAuthorized() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");

    ApiResponse result =
        topicSyncControllerService.updateSyncTopics(utilMethods.getSyncTopicUpdates());
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(4)
  public void getSyncTopics() throws Exception {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterApiTopics("topic", 10));
    when(handleDbRequests.getAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");

    SyncTopicsList topicRequests =
        topicSyncControllerService.getSyncTopics(
            envSel, pageNo, "", topicNameSearch, "false", false);
    assertThat(topicRequests.getResultSet()).isNotNull();
  }

  @Test
  @Order(5)
  public void approveTopicRequestAllTopicsWhereOneTopicIsAlreadyCreated() throws KlawException {
    // mock
    stubUserInfo();
    mockMultipleTopics();
    mockGetTopicsFromEnv();
    when(clusterApiService.approveTopicRequests(
            any(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            any(),
            eq(TENANT_ID),
            anyBoolean()))
        .thenReturn(createAPIResponse(ApiResultStatus.SUCCESS.value))
        .thenReturn(
            createAPIResponse(
                "org.apache.kafka.common.errors.TopicExistsException: Topic 'testtopic' already exists."));
    when(handleDbRequests.updateTopicRequest(any(), any()))
        .thenReturn(
            CRUDResponse.<Topic>builder()
                .resultStatus(ApiResultStatus.SUCCESS.value)
                .entities(List.of(new Topic()))
                .build());
    // execute
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(ALL_TOPICS, new String[0]));

    // verfiy only 1 topic Request is sent to the manage database as the other already exists.
    verify(handleDbRequests).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests).updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));
    verify(handleDbRequests, times(1)).updateTopicRequest(any(), any());
    List<TopicRequest> req = topicRequestCaptor.getAllValues();
    List<TopicRequest> update = updateTopicRequestCaptor.getAllValues();
    // we have two topics but one already exists so only one should be added to the database.
    assertThat(req.size()).isEqualTo(1);
    assertThat(update.size()).isEqualTo(1);
    // we expect all request to also be updated to approved
    assertThat(req.size()).isEqualTo(update.size());

    verifyCaptureContents(req, update, 0, 1, TOPIC_NAME_1);

    // This should pass when clusterApi is updated
    assertThat(retval.getMessage())
        .isNotEqualTo("Error :Could not approve topic request. Please contact Administrator.");
    assertThat(retval.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(6)
  public void approveTopicRequestAllTopicsCreateAll() throws KlawException {

    stubUserInfo();
    mockMultipleTopics();
    mockGetTopicsFromEnv();
    when(clusterApiService.approveTopicRequests(
            any(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            any(),
            eq(TENANT_ID),
            anyBoolean()))
        .thenReturn(createAPIResponse(ApiResultStatus.SUCCESS.value));
    when(handleDbRequests.updateTopicRequest(any(), any()))
        .thenReturn(
            CRUDResponse.<Topic>builder()
                .resultStatus(ApiResultStatus.SUCCESS.value)
                .entities(List.of(new Topic()))
                .build());

    // execute
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(ALL_TOPICS, new String[0]));

    // verfiy exactly 2 topics Requests are sent to the manage database
    verify(handleDbRequests, times(2)).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests, times(2))
        .updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));
    verify(handleDbRequests, times(2)).updateTopicRequest(any(), any());
    List<TopicRequest> req = topicRequestCaptor.getAllValues();
    List<TopicRequest> update = updateTopicRequestCaptor.getAllValues();
    // we have two new topics both should be added to the database.
    assertThat(req.size()).isEqualTo(2);
    assertThat(update.size()).isEqualTo(2);
    // we expect all request to also be updated to approved
    verifyCaptureContents(req, update, 0, 1, TOPIC_NAME_1);

    verifyCaptureContents(req, update, 1, 2, TOPIC_NAME_2);
    assertThat(retval.getMessage())
        .isNotEqualTo("Error :Could not approve topic request. Please contact Administrator.");
    assertThat(retval.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(7)
  public void approveTopicRequestSelectedWhereOneTopicIsAlreadyCreated() throws KlawException {
    stubUserInfo();
    mockMultipleTopics();
    mockSelectedOnlyTopics(1, TOPIC_NAME_1, env.getId());

    mockSelectedOnlyTopics(2, TOPIC_NAME_2, test.getId());
    when(clusterApiService.approveTopicRequests(
            any(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            any(),
            eq(TENANT_ID),
            anyBoolean()))
        .thenReturn(createAPIResponse(ApiResultStatus.SUCCESS.value))
        .thenReturn(
            createAPIResponse(
                "org.apache.kafka.common.errors.TopicExistsException: Topic 'testtopic' already exists."));
    when(handleDbRequests.updateTopicRequest(any(), any()))
        .thenReturn(
            CRUDResponse.<Topic>builder()
                .resultStatus(ApiResultStatus.SUCCESS.value)
                .entities(List.of(new Topic()))
                .build());
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(SELECTED_TOPICS, new String[] {"1", "2"}));

    // verfiy only 1 topic Request is sent to the manage database
    verify(handleDbRequests).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests).updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));
    verify(handleDbRequests, times(1)).updateTopicRequest(any(), any());
    List<TopicRequest> req = topicRequestCaptor.getAllValues();
    List<TopicRequest> update = updateTopicRequestCaptor.getAllValues();
    // we have two topics but one already exists so only one should be added to the database.
    assertThat(req.size()).isEqualTo(1);
    assertThat(update.size()).isEqualTo(1);
    // we expect all request to also be updated to approved
    assertThat(req.size()).isEqualTo(update.size());

    verifyCaptureContents(req, update, 0, 1, TOPIC_NAME_1);

    assertThat(retval.getMessage())
        .isNotEqualTo("Error :Could not approve topic request. Please contact Administrator.");
    assertThat(retval.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(8)
  public void approveTopicRequestSelectedCreateAll() throws KlawException {

    stubUserInfo();
    mockMultipleTopics();
    mockSelectedOnlyTopics(1, TOPIC_NAME_1, env.getId());

    mockSelectedOnlyTopics(2, TOPIC_NAME_2, test.getId());
    when(clusterApiService.approveTopicRequests(
            any(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            any(),
            eq(TENANT_ID),
            anyBoolean()))
        .thenReturn(createAPIResponse(ApiResultStatus.SUCCESS.value));
    when(handleDbRequests.updateTopicRequest(any(), any()))
        .thenReturn(
            CRUDResponse.<Topic>builder()
                .resultStatus(ApiResultStatus.SUCCESS.value)
                .entities(List.of(new Topic()))
                .build());
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(SELECTED_TOPICS, new String[] {"1", "2"}));

    // verfiy only 1 topic Request is sent to the manage database
    verify(handleDbRequests, times(2)).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests, times(2))
        .updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));
    verify(handleDbRequests, times(2)).updateTopicRequest(any(), any());
    List<TopicRequest> req = topicRequestCaptor.getAllValues();
    List<TopicRequest> update = updateTopicRequestCaptor.getAllValues();
    // we have two new topics both should be added to the database.
    assertThat(req.size()).isEqualTo(2);
    assertThat(update.size()).isEqualTo(2);
    // we expect all request to also be updated to approved
    assertThat(req.size()).isEqualTo(update.size());

    verifyCaptureContents(req, update, 0, 1, TOPIC_NAME_1);
    verifyCaptureContents(req, update, 1, 2, TOPIC_NAME_2);
    assertThat(retval.getMessage())
        .isNotEqualTo("Error :Could not approve topic request. Please contact Administrator.");
    assertThat(retval.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(9)
  public void approveTopicRequestSelectedUnexpectedExceptionFromClusterApi() throws KlawException {
    stubUserInfo();
    mockMultipleTopics();
    mockSelectedOnlyTopics(1, TOPIC_NAME_1, env.getId());

    mockSelectedOnlyTopics(2, TOPIC_NAME_2, test.getId());
    when(clusterApiService.approveTopicRequests(
            any(),
            anyString(),
            anyInt(),
            anyString(),
            anyString(),
            any(),
            eq(TENANT_ID),
            anyBoolean()))
        .thenReturn(createAPIResponse(ApiResultStatus.SUCCESS.value))
        .thenThrow(
            new KlawException("Could not approve topic request. Please contact Administrator."));
    when(handleDbRequests.updateTopicRequest(any(), any()))
        .thenReturn(
            CRUDResponse.<Topic>builder()
                .resultStatus(ApiResultStatus.SUCCESS.value)
                .entities(List.of(new Topic()))
                .build());
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(SELECTED_TOPICS, new String[] {"1", "2"}));

    // verfiy only 1 topic Request is sent to the manage database
    verify(handleDbRequests).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests).updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));
    verify(handleDbRequests, times(1)).updateTopicRequest(any(), any());
    List<TopicRequest> req = topicRequestCaptor.getAllValues();
    List<TopicRequest> update = updateTopicRequestCaptor.getAllValues();
    // we have two topics but one already exists so only one should be added to the database.
    assertThat(req.size()).isEqualTo(1);
    assertThat(update.size()).isEqualTo(1);
    // we expect all request to also be updated to approved
    assertThat(req.size()).isEqualTo(update.size());

    verifyCaptureContents(req, update, 0, 1, TOPIC_NAME_1);

    assertThat(retval.getMessage())
        .isEqualTo("Error :Could not approve topic request. Please contact Administrator.");
  }

  @Test
  @Order(10)
  public void getSyncList_noValidationSet_base() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(14);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(14));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics("1", "1", "", null, "false", false);

    // 14 in the DB and 14 in the cluster means we return 0 here.
    assertThat(syncTopics.getResultSet()).hasSize(0);
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      assertThat(response.getValidationStatus()).isEqualTo(null);
      assertThat(response.isValidatedTopic()).isTrue();
    }
  }

  @Test
  @Order(11)
  public void getReconSyncList_noValidationSet_base() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(14);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(14));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getReconTopics("1", "1", "", null, "false", false);

    // 14 in the DB and 14 in the cluster means we return 0 here.
    assertThat(syncTopics.getResultSet()).hasSize(0);
  }

  @Test
  @Order(12)
  public void getSyncList_noValidationSet_TwoNotSynchronized() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(13);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(15));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics("1", "1", "", null, "false", false);

    // With 13 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(2);

    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      assertThat(response.getValidationStatus()).isEqualTo(null);
      assertThat(response.isValidatedTopic()).isTrue();
    }
  }

  @Test
  @Order(13)
  public void getReconSyncList_noValidationSet_FiveNotSynched() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(14);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(19));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getReconTopics("1", "1", "", null, "false", false);

    // 14 in the DB and 14 in the cluster means we return 0 here.
    assertThat(syncTopics.getResultSet()).hasSize(5);
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      assertThat(response.getValidationStatus()).isEqualTo(null);
      assertThat(response.isValidatedTopic()).isTrue();
    }
  }

  @Test
  @Order(14)
  public void getSyncList_noValidationSet_ThreeDeletedFromCluster_allTopics() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(12);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(15));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics("1", "1", "", null, "true", false);

    // With 12 existing in the DB and 15 on the cluster all 15 are returned
    assertThat(syncTopics.getResultSet()).hasSize(15);

    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      assertThat(response.getValidationStatus()).isEqualTo(null);
    }
  }

  @Test
  @Order(15)
  public void getReconSyncList_noValidationSet_FourDeletedFromCluster() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(14);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(10));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getReconTopics("1", "1", "", null, "false", false);

    // 14 in the DB and 10 in the cluster i am expecting the difference to be returned
    assertThat(syncTopics.getResultSet()).hasSize(4);
    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      assertThat(response.getValidationStatus()).isEqualTo(null);
    }
  }

  @Test
  @Order(16)
  public void getSyncList_noValidationSet_ThreeDeletedFromCluster() throws Exception {
    stubUserInfo();
    environmentSetUp();
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(1));
    List<Topic> topics = utilMethods.generateTopics(12);

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(15));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics("1", "1", "", null, "false", false);

    // With 12 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(3);

    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      assertThat(response.getValidationStatus()).isEqualTo(null);
    }
  }

  /**
   * @param numberOfTopicsInDB How many topics already exist in the Klaw DB
   * @param numberOfTopicsInCluster How Many topics Exist on the cluster
   * @param environment Which environment you want to run against (check validatedEnvironmentSetUp()
   *     to see the different envs and there individual validation setups
   * @param expectedReturned How many Topics should be returned in the SyncTopicResponse
   * @param expectedInValid How many of the returned Topics will have invlaid Topic Names
   * @param topicNames A space sperated list that will be the names of the topics returned in the DB
   *     and Cluster.
   * @throws Exception
   */
  @ParameterizedTest
  @CsvSource({
    "5,8,1,3,3,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 DevTopic6 DevTopic7 DevTopic8",
    "5,8,1,3,0,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8",
    "8,8,1,0,0,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8",
    "3,8,2,5,2,Topic1 Topic2 Topic3 Topic4 Topic5-TST Topic6-tst Topic7-TST Topic8-TST",
    "3,8,3,5,4,Topic1 Topic2 Topic3 Topic4 Topic5-TST Topic6-tst Topic7-TST Topic-UAT-8",
    "3,8,3,5,1,Topic1 Topic2 Topic3 Topic-4 Topic-UAT-5 Topic-UAT-6 Topic-UAT-7 Topic-UAT-8",
    "7,8,4,1,1,Topic1 Topic2 Topic3 Topic-4 Topic-UAT-5 Topic-UAT-6 Topic-UAT-7 Topic-UAT-8",
    "1,8,4,7,4,Topic1 Topic2 prd-Topic3-prd PRD-Topic-4-PRD prd-Topic-UAT-5 prd-Topic-6-prd prd-Topic-7-prd Topic-PRD-8"
  })
  @Order(17)
  public void getSyncList_ValidationOn(
      int numberOfTopicsInDB,
      int numberOfTopicsInCluster,
      int environment,
      int expectedReturned,
      int expectedInValid,
      String topicNames)
      throws Exception {
    String[] namesOfTopics = topicNames.split(" ");
    stubUserInfo();
    validatedEnvironmentSetUp("3", "9");
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(4));
    List<Topic> topics =
        utilMethods.generateTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInDB));

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(
            generateClusterTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInCluster)));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics(
            String.valueOf(environment), "1", "", null, "false", false);

    // With 12 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(expectedReturned);
    int actualInvalid = 0, actualStringValidation = 0;
    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      if (!response.isValidatedTopic() && !response.getRemarks().equals("DELETED")) {
        actualInvalid++;
      }
      if (!StringUtils.isEmpty(response.getValidationStatus())) {
        actualStringValidation++;
      }
    }
    // every invalid string shoult match with an invalid flag
    assertThat(actualInvalid).isEqualTo(actualStringValidation);
    assertThat(actualInvalid).isEqualTo(expectedInValid);
    assertThat(actualStringValidation).isEqualTo(expectedInValid);
  }

  /**
   * @param numberOfTopicsInDB How many topics already exist in the Klaw DB
   * @param numberOfTopicsInCluster How Many topics Exist on the cluster
   * @param environment Which environment you want to run against (check validatedEnvironmentSetUp()
   *     to see the different envs and there individual validation setups
   * @param expectedReturned How many Topics should be returned in the SyncTopicResponse
   * @param expectedInValid How many of the returned Topics will have invlaid Topic Names
   * @param topicNames A space sperated list that will be the names of the topics returned in the DB
   *     and Cluster.
   * @throws Exception
   */
  @ParameterizedTest
  @CsvSource({
    "5,8,1,3,3,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 DevTopic6 DevTopic7 DevTopic8",
    "5,8,1,3,0,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8",
    "8,6,1,2,0,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8",
    "3,8,2,5,2,Topic1 Topic2 Topic3 Topic4 Topic5-TST Topic6-tst Topic7-TST Topic8-TST",
    "3,8,3,5,4,Topic1 Topic2 Topic3 Topic4 Topic5-TST Topic6-tst Topic7-TST Topic-UAT-8",
    "3,8,3,5,1,Topic1 Topic2 Topic3 Topic-4 Topic-UAT-5 Topic-UAT-6 Topic-UAT-7 Topic-UAT-8",
    "7,8,4,1,1,Topic1 Topic2 Topic3 Topic-4 Topic-UAT-5 Topic-UAT-6 Topic-UAT-7 Topic-UAT-8",
    "1,8,4,7,4,Topic1 Topic2 prd-Topic3-prd PRD-Topic-4-PRD prd-Topic-UAT-5 prd-Topic-6-prd prd-Topic-7-prd Topic-PRD-8"
  })
  @Order(18)
  public void getReconSyncList_ValidationOn(
      int numberOfTopicsInDB,
      int numberOfTopicsInCluster,
      int environment,
      int expectedReturned,
      int expectedInValid,
      String topicNames)
      throws Exception {
    String[] namesOfTopics = topicNames.split(" ");
    stubUserInfo();
    validatedEnvironmentSetUp("3", "9");
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(4));
    List<Topic> topics =
        utilMethods.generateTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInDB));

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(
            generateClusterTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInCluster)));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getReconTopics(
            String.valueOf(environment), "1", "", null, "false", false);

    // With 12 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(expectedReturned);
    int actualInvalid = 0, actualStringValidation = 0;
    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      if (!response.isValidatedTopic() && !response.getRemarks().equals("DELETED")) {
        actualInvalid++;
      }
      if (!StringUtils.isEmpty(response.getValidationStatus())) {
        actualStringValidation++;
      }
    }
    // every invalid string shoult match with an invalid flag
    assertThat(actualInvalid).isEqualTo(actualStringValidation);
    assertThat(actualInvalid).isEqualTo(expectedInValid);
    assertThat(actualStringValidation).isEqualTo(expectedInValid);
  }

  /**
   * @param numberOfTopicsInDB How many topics already exist in the Klaw DB
   * @param numberOfTopicsInCluster How Many topics Exist on the cluster
   * @param environment Which environment you want to run against (check validatedEnvironmentSetUp()
   *     to see the different envs and there individual validation setups
   * @param expectedReturned How many Topics should be returned in the SyncTopicResponse
   * @param expectedInValid How many of the returned Topics will have invlaid Topic Names
   * @param topicNames A space sperated list that will be the names of the topics returned in the DB
   *     and Cluster.
   * @throws Exception
   */
  @ParameterizedTest
  @CsvSource({
    "5,8,1,8,7,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 DevTopic6 DevTopic7 DevTopic8",
    "0,3,1,3,3,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8",
    "8,6,1,8,4,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8",
    "3,5,2,5,4,Topic1 Topic2 Topic3 Topic4 Topic5-TST Topic6-tst Topic7-TST Topic8-TST",
    "3,7,3,7,7,Topic1 Topic2 Topic3 Topic4 Topic5-TST Topic6-tst Topic7-TST Topic-UAT-8",
    "3,8,3,8,4,Topic1 Topic2 Topic3 Topic-4 Topic-UAT-5 Topic-UAT-6 Topic-UAT-7 Topic-UAT-8",
    "7,8,4,8,8,Topic1 Topic2 Topic3 Topic-4 Topic-UAT-5 Topic-UAT-6 Topic-UAT-7 Topic-UAT-8",
    "1,7,4,7,4,Topic1 Topic2 prd-Topic3-prd PRD-Topic-4-PRD prd-Topic-UAT-5 prd-Topic-6-prd prd-Topic-7-prd Topic-PRD-8"
  })
  @Order(19)
  public void getSyncList_ValidationOn_AllTopics(
      int numberOfTopicsInDB,
      int numberOfTopicsInCluster,
      int environment,
      int expectedReturned,
      int expectedInValid,
      String topicNames)
      throws Exception {
    String[] namesOfTopics = topicNames.split(" ");
    stubUserInfo();
    validatedEnvironmentSetUp("3", "9");
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(4));
    List<Topic> topics =
        utilMethods.generateTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInDB));

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(
            generateClusterTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInCluster)));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics(
            String.valueOf(environment), "1", "", null, "true", false);

    // With 12 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(expectedReturned);
    int actualInvalid = 0, actualStringValidation = 0;
    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      if (!response.isValidatedTopic()
          && (response.getRemarks() == null || !response.getRemarks().equals("DELETED"))) {
        actualInvalid++;
      }
      if (!StringUtils.isEmpty(response.getValidationStatus())) {
        actualStringValidation++;
      }
    }
    // every invalid string shoult match with an invalid flag
    assertThat(actualInvalid).isEqualTo(actualStringValidation);
    assertThat(actualInvalid).isEqualTo(expectedInValid);
    assertThat(actualStringValidation).isEqualTo(expectedInValid);
  }

  /**
   * @Param maxRepFactor The maximum Replication Factor set on the environment @Param maxPartitions
   * The maximum number of partitions allowed on the environment
   *
   * @param numberOfTopicsInDB How many topics already exist in the Klaw DB
   * @param numberOfTopicsInCluster How Many topics Exist on the cluster
   * @param environment Which environment you want to run against (check validatedEnvironmentSetUp()
   *     to see the different envs and there individual validation setups
   * @param expectedReturned How many Topics should be returned in the SyncTopicResponse
   * @param expectedInValid How many of the returned Topics will have invlaid Topic Names
   * @param topicNames A space sperated list that will be the names of the topics returned in the DB
   *     and Cluster.
   * @throws Exception
   */
  @ParameterizedTest
  @CsvSource({
    "0,9,5,8,1,3,3,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 DevTopic6 DevTopic7 DevTopic8,Topic exceeds maximum replication factor 0 with 3 configured replication factor. ",
    "1,1,0,3,1,3,3,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8,Topic exceeds maximum partitions 1 with 9 configured partitions. ",
    "1,1,8,6,1,2,0,Topic1 Topic2 Topic3 Topic4 Dev-Topic5 Dev-Topic6 Dev-Topic7 Dev-Topic8,No error expected because there are two topics to be deleted returned",
  })
  @Order(20)
  public void getSyncList_FailedValidationOn_ReplicationAndPartitions(
      String maxRepFactor,
      String maxPartitions,
      int numberOfTopicsInDB,
      int numberOfTopicsInCluster,
      int environment,
      int expectedReturned,
      int expectedInValid,
      String topicNames,
      String expectedErrorMsg)
      throws Exception {
    String[] namesOfTopics = topicNames.split(" ");
    stubUserInfo();
    validatedEnvironmentSetUp(maxRepFactor, maxPartitions);
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(4));
    List<Topic> topics =
        utilMethods.generateTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInDB));

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(
            generateClusterTopics(Arrays.copyOfRange(namesOfTopics, 0, numberOfTopicsInCluster)));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics(
            String.valueOf(environment), "1", "", null, "false", false);

    // With 12 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(expectedReturned);
    int actualInvalid = 0, actualStringValidation = 0;
    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      if (!response.isValidatedTopic()
          && (response.getRemarks() == null || !response.getRemarks().equals("DELETED"))) {
        actualInvalid++;
      }
      if (!StringUtils.isEmpty(response.getValidationStatus())) {
        actualStringValidation++;
        assertThat(response.getValidationStatus()).contains(expectedErrorMsg);
      }
    }
    // every invalid string shoult match with an invalid flag
    assertThat(actualInvalid).isEqualTo(actualStringValidation);
    assertThat(actualInvalid).isEqualTo(expectedInValid);
    assertThat(actualStringValidation).isEqualTo(expectedInValid);
  }

  @Test
  @Order(21)
  public void getSyncList_FailedValidationOn_ReplicationAndPartitionsAreNull() throws Exception {
    String[] namesOfTopics = new String[] {"Topic1", "Topic2", "Topic3", "Topic4"};
    stubUserInfo();
    validatedEnvironmentSetUp(null, null);
    // Get KwCluster Settings
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA), eq(101)))
        .thenReturn(getKwClusters(4));
    List<Topic> topics = utilMethods.generateTopics(Arrays.copyOfRange(namesOfTopics, 0, 1));

    // from the cluster
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString(), eq(101)))
        .thenReturn(generateClusterTopics(Arrays.copyOfRange(namesOfTopics, 0, 4)));

    // from the DB
    when(handleDbRequests.getSyncTopics(eq("1"), eq(null), eq(101))).thenReturn(topics);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topics);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Team1");

    SyncTopicsList syncTopics =
        topicSyncControllerService.getSyncTopics("1", "1", "", null, "false", false);

    // With 12 existing in the DB and 15 on the cluster the missing 2 are returned
    assertThat(syncTopics.getResultSet()).hasSize(3);
    int actualInvalid = 0, actualStringValidation = 0;
    // Deleted Topics are not set with validation status as they are being removed
    for (TopicSyncResponseModel response : syncTopics.getResultSet()) {
      if (!response.isValidatedTopic()
          && (response.getRemarks() == null || !response.getRemarks().equals("DELETED"))) {
        actualInvalid++;
      }
      if (!StringUtils.isEmpty(response.getValidationStatus())) {
        actualStringValidation++;
        //        assertThat(response.getValidationStatus()).contains(expectedErrorMsg);
      }
    }
    // every invalid string shoult match with an invalid flag
    assertThat(actualInvalid).isEqualTo(actualStringValidation);
    assertThat(actualInvalid).isEqualTo(3);
    assertThat(actualStringValidation).isEqualTo(3);
  }

  private List<TopicConfig> generateClusterTopics(int numberOfTopics) {
    String[] topicNames = new String[numberOfTopics];
    for (int i = 0; i < numberOfTopics; i++) {
      topicNames[i] = "Topic" + i;
    }
    return generateClusterTopics(topicNames);
  }

  private List<TopicConfig> generateClusterTopics(String... topicNames) {

    List<TopicConfig> topics = new ArrayList<>();

    for (int i = 0; i < topicNames.length; i++) {
      TopicConfig topic = new TopicConfig();
      topic.setTopicName(topicNames[i]);
      topic.setPartitions("9");
      topic.setReplicationFactor("3");
      topics.add(topic);
    }
    return topics;
  }

  private Map<Integer, KwClusters> getKwClusters(int numberOfClusters) {

    Map<Integer, KwClusters> clustersMap = new HashMap<>();
    for (int i = 1; i <= numberOfClusters; i++) {
      KwClusters cluster = new KwClusters();

      cluster.setClusterId(i);
      cluster.setTenantId(101);
      cluster.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA.value);
      cluster.setBootstrapServers("localhost:9092");
      cluster.setClusterName("UnitTest");
      cluster.setClusterType("kafka");
      cluster.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
      clustersMap.put(i, cluster);
    }
    return clustersMap;
  }

  private List<Team> getAvailableTeams() {

    Team team1 = new Team();
    team1.setTeamname("Team1");

    Team team2 = new Team();
    team2.setTeamname("Team2");

    Team team3 = new Team();
    team3.setTeamname("Team3");

    List<Team> teamList = new ArrayList<>();
    teamList.add(team1);
    teamList.add(team2);
    teamList.add(team3);

    return teamList;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn(USERNAME);
    when(commonUtilsService.getTenantId(eq(USERNAME))).thenReturn(TENANT_ID);
    when(handleDbRequests.getAllTeams(eq(101))).thenReturn(getAvailableTeams());
    /// added
    when(manageDatabase.getTeamIdFromTeamName(eq(101), eq("Team1"))).thenReturn(10);
    when(manageDatabase.getTeamIdFromTeamName(eq(101), eq("Team2"))).thenReturn(20);
    when(manageDatabase.getTeamIdFromTeamName(eq(101), eq("Team3"))).thenReturn(30);
  }

  private SyncBackTopics createSyncBackTopic(String TypeOfSync, String[] topics) {
    SyncBackTopics syncBackTopics = new SyncBackTopics();
    syncBackTopics.setSourceEnv("1");
    syncBackTopics.setTargetEnv("2");
    syncBackTopics.setTypeOfSync(TypeOfSync);
    syncBackTopics.setTopicIds(topics);
    return syncBackTopics;
  }

  private Topic createTopic(Integer topicId, String topicName, String EnvId) {

    Topic topic = new Topic();
    topic.setTeamId(topicId);
    topic.setTopicname(topicName);
    topic.setEnvironment(EnvId);
    topic.setNoOfPartitions(2);
    topic.setNoOfReplicas("1");
    topic.setTeamId(TENANT_ID);
    return topic;
  }

  private ResponseEntity<ApiResponse> createAPIResponse(String resultStatus) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.builder().message(resultStatus).build());
  }

  private void mockMultipleTopics() {

    when(handleDbRequests.requestForTopic(any()))
        .thenReturn(
            new HashMap<String, String>() {
              {
                put("result", ApiResultStatus.SUCCESS.value);
                put("topicId", "1");
              }
            })
        .thenReturn(
            new HashMap<String, String>() {
              {
                put("result", ApiResultStatus.SUCCESS.value);
                put("topicId", "2");
              }
            });
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(createTopic(Integer.valueOf(1), TOPIC_NAME_1, env.getId())))
        .thenReturn(List.of(createTopic(Integer.valueOf(2), TOPIC_NAME_2, env.getId())));
  }

  private void mockGetTopicsFromEnv() {
    when(handleDbRequests.getTopicsFromEnv(env.getId(), TENANT_ID))
        .thenReturn(
            List.of(
                createTopic(Integer.valueOf(1), TOPIC_NAME_1, env.getId()),
                createTopic(Integer.valueOf(2), TOPIC_NAME_2, test.getId())));
  }

  private void mockSelectedOnlyTopics(int topicId, String topicName, String envId) {
    when(handleDbRequests.getTopicFromId(eq(topicId), eq(TENANT_ID)))
        .thenReturn(Optional.of(createTopic(Integer.valueOf(topicId), topicName, envId)));
  }

  private void verifyCaptureContents(
      List<TopicRequest> req, List<TopicRequest> update, int entry, int topicId, String topicName) {
    assertThat(req.size()).isEqualTo(update.size());
    assertThat(req.get(entry).getTopicid()).isEqualTo(update.get(entry).getTopicid());
    // Check it is the expected topic.
    TopicRequest topicReq = req.get(entry);
    assertThat(topicReq.getEnvironment()).isEqualTo(test.getId());
    assertThat(topicReq.getTopicid()).isEqualTo(topicId);
    assertThat(topicReq.getTopicname()).isEqualTo(topicName);
  }
}
