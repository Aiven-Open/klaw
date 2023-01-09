package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.*;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.*;
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

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

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
    EnvironmentSetUp();

    ReflectionTestUtils.setField(topicSyncControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(topicSyncControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        topicSyncControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        topicSyncControllerService, "clusterApiService", clusterApiService);
    ReflectionTestUtils.setField(
        topicSyncControllerService,
        "rolesPermissionsControllerService",
        rolesPermissionsControllerService);

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  private void EnvironmentSetUp() {
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
    assertThat(result.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
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
    assertThat(result.getResult()).isEqualTo("No record updated.");
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
    assertThat(result.getResult()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(4)
  public void getSyncTopics() throws Exception {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAllTopics(
            anyString(), any(KafkaSupportedProtocol.class), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterApiTopics("topic", 10));
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(rolesPermissionsControllerService.getApproverRoles(anyString(), anyInt()))
        .thenReturn(List.of("USER"));

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
    MockMultipleTopics();
    mockGetTopicsFromEnv();
    when(clusterApiService.approveTopicRequests(
            any(), anyString(), anyInt(), anyString(), anyString(), any(), eq(TENANT_ID)))
        .thenReturn(createAPIResponse(HttpStatus.OK, ApiResultStatus.SUCCESS.value))
        .thenReturn(
            createAPIResponse(
                HttpStatus.OK,
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
    assertEquals(1, req.size());
    assertEquals(1, update.size());
    // we expect all request to also be updated to approved
    assertEquals(req.size(), update.size());

    verifyCaptureContents(req, update, 0, 1, "UnitTest-01");

    // This should pass when clusterApi is updated
    assertNotEquals(
        "Error :Could not approve topic request. Please contact Administrator.",
        retval.getResult());
    assertEquals("success", retval.getResult());
  }

  @Test
  @Order(6)
  public void approveTopicRequestAllTopicsCreateAll() throws KlawException {

    stubUserInfo();
    MockMultipleTopics();
    mockGetTopicsFromEnv();
    when(clusterApiService.approveTopicRequests(
            any(), anyString(), anyInt(), anyString(), anyString(), any(), eq(TENANT_ID)))
        .thenReturn(createAPIResponse(HttpStatus.OK, ApiResultStatus.SUCCESS.value));

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
    assertEquals(2, req.size());
    assertEquals(2, update.size());
    // we expect all request to also be updated to approved
    verifyCaptureContents(req, update, 0, 1, "UnitTest-01");

    verifyCaptureContents(req, update, 1, 2, "UnitTest-02");
    assertNotEquals(
        "Error :Could not approve topic request. Please contact Administrator.",
        retval.getResult());
    assertEquals("success", retval.getResult());
  }

  @Test
  @Order(7)
  public void approveTopicRequestSelectedWhereOneTopicIsAlreadyCreated() throws KlawException {
    stubUserInfo();
    MockMultipleTopics();
    mockSelectedOnlyTopics(1, "UnitTest-01", env.getId());

    mockSelectedOnlyTopics(2, "UnitTest-02", test.getId());
    when(clusterApiService.approveTopicRequests(
            any(), anyString(), anyInt(), anyString(), anyString(), any(), eq(TENANT_ID)))
        .thenReturn(createAPIResponse(HttpStatus.OK, ApiResultStatus.SUCCESS.value))
        .thenReturn(
            createAPIResponse(
                HttpStatus.OK,
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
    assertEquals(1, req.size());
    assertEquals(1, update.size());
    // we expect all request to also be updated to approved
    assertEquals(req.size(), update.size());

    verifyCaptureContents(req, update, 0, 1, "UnitTest-01");

    assertNotEquals(
        "Error :Could not approve topic request. Please contact Administrator.",
        retval.getResult());
    assertEquals("success", retval.getResult());
  }

  @Test
  @Order(8)
  public void approveTopicRequestSelectedCreateAll() throws KlawException {

    stubUserInfo();
    MockMultipleTopics();
    mockSelectedOnlyTopics(1, "UnitTest-01", env.getId());

    mockSelectedOnlyTopics(2, "UnitTest-02", test.getId());
    when(clusterApiService.approveTopicRequests(
            any(), anyString(), anyInt(), anyString(), anyString(), any(), eq(TENANT_ID)))
        .thenReturn(createAPIResponse(HttpStatus.OK, ApiResultStatus.SUCCESS.value));

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
    assertEquals(2, req.size());
    assertEquals(2, update.size());
    // we expect all request to also be updated to approved
    assertEquals(req.size(), update.size());

    verifyCaptureContents(req, update, 0, 1, "UnitTest-01");
    verifyCaptureContents(req, update, 1, 2, "UnitTest-02");
    assertNotEquals(
        "Error :Could not approve topic request. Please contact Administrator.",
        retval.getResult());
    assertEquals("success", retval.getResult());
  }

  @Test
  @Order(9)
  public void approveTopicRequestSelectedUnexpectedExceptionFromClusterApi() throws KlawException {
    stubUserInfo();
    MockMultipleTopics();
    mockSelectedOnlyTopics(1, "UnitTest-01", env.getId());

    mockSelectedOnlyTopics(2, "UnitTest-02", test.getId());
    when(clusterApiService.approveTopicRequests(
            any(), anyString(), anyInt(), anyString(), anyString(), any(), eq(TENANT_ID)))
        .thenReturn(createAPIResponse(HttpStatus.OK, ApiResultStatus.SUCCESS.value))
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
    assertEquals(1, req.size());
    assertEquals(1, update.size());
    // we expect all request to also be updated to approved
    assertEquals(req.size(), update.size());

    verifyCaptureContents(req, update, 0, 1, "UnitTest-01");

    assertEquals(
        "Error :Could not approve topic request. Please contact Administrator.",
        retval.getResult());
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
    topic.setNoOfReplcias("1");
    topic.setTeamId(TENANT_ID);
    return topic;
  }

  private ResponseEntity<ApiResponse> createAPIResponse(HttpStatus status, String resultStatus) {
    return ResponseEntity.status(status)
        .body(ApiResponse.builder().status(status).result(resultStatus).build());
  }

  private void MockMultipleTopics() {

    when(handleDbRequests.requestForTopic(any()))
        .thenReturn(
            new HashMap<String, String>() {
              {
                put("result", "success");
                put("topicId", "1");
              }
            })
        .thenReturn(
            new HashMap<String, String>() {
              {
                put("result", "success");
                put("topicId", "2");
              }
            });
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(createTopic(Integer.valueOf(1), "UnitTest-01", env.getId())))
        .thenReturn(List.of(createTopic(Integer.valueOf(2), "UnitTest-02", env.getId())));
  }

  private void mockGetTopicsFromEnv() {
    when(handleDbRequests.getTopicsFromEnv(env.getId(), TENANT_ID))
        .thenReturn(
            List.of(
                createTopic(Integer.valueOf(1), "UnitTest-01", env.getId()),
                createTopic(Integer.valueOf(2), "UnitTest-02", test.getId())));
  }

  private void mockSelectedOnlyTopics(int topicId, String topicName, String envId) {
    when(handleDbRequests.getTopicFromId(eq(topicId), eq(TENANT_ID)))
        .thenReturn(Optional.of(createTopic(Integer.valueOf(topicId), topicName, envId)));
  }

  private void verifyCaptureContents(
      List<TopicRequest> req, List<TopicRequest> update, int entry, int topicId, String topicName) {
    assertEquals(req.size(), update.size());
    assertEquals(req.get(entry).getTopicid(), update.get(entry).getTopicid());
    // Check it is the expected topic.
    TopicRequest topicReq = req.get(entry);
    assertEquals(test.getId(), topicReq.getEnvironment());
    assertEquals(topicId, topicReq.getTopicid());
    assertEquals(topicName, topicReq.getTopicname());
  }
}
