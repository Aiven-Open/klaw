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
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import java.util.ArrayList;
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
    env.setName("DEV");

    test.setId("2");
    test.setName("TST");
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(List.of(env, test));
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
    when(handleDbRequests.addToSynctopics(any())).thenReturn(ApiResultStatus.SUCCESS.value);

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
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

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
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");

    Map<String, Object> topicRequests =
        topicSyncControllerService.getSyncTopics(
            envSel, pageNo, "", topicNameSearch, "false", false);
    assertThat(topicRequests).hasSize(2);
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

    // execute
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(ALL_TOPICS, new String[0]));

    // verfiy only 1 topic Request is sent to the manage database as the other already exists.
    verify(handleDbRequests).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests).updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));

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

    // execute
    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(ALL_TOPICS, new String[0]));

    // verfiy exactly 2 topics Requests are sent to the manage database
    verify(handleDbRequests, times(2)).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests, times(2))
        .updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));

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

    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(SELECTED_TOPICS, new String[] {"1", "2"}));

    // verfiy only 1 topic Request is sent to the manage database
    verify(handleDbRequests).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests).updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));

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

    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(SELECTED_TOPICS, new String[] {"1", "2"}));

    // verfiy only 1 topic Request is sent to the manage database
    verify(handleDbRequests, times(2)).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests, times(2))
        .updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));

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

    ApiResponse retval =
        topicSyncControllerService.updateSyncBackTopics(
            createSyncBackTopic(SELECTED_TOPICS, new String[] {"1", "2"}));

    // verfiy only 1 topic Request is sent to the manage database
    verify(handleDbRequests).requestForTopic(topicRequestCaptor.capture());
    verify(handleDbRequests).updateTopicRequest(updateTopicRequestCaptor.capture(), eq(USERNAME));

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
