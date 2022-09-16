package io.aiven.klaw.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.*;
import java.sql.Timestamp;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class TopicControllerServiceTest {

  @Mock private ClusterApiService clusterApiService;

  @Mock private UserDetails userDetails;

  @Mock private UserInfo userInfo;

  @Mock private HashMap<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

  @Mock private ManageDatabase manageDatabase;

  @Mock private HandleDbRequests handleDbRequests;

  @Mock CommonUtilsService commonUtilsService;

  @Mock private MailUtils mailService;

  private TopicControllerService topicControllerService;

  private TopicSyncControllerService topicSyncControllerService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  @Mock HashMap<Integer, KwTenantConfigModel> tenantConfig;

  @Mock KwTenantConfigModel tenantConfigModel;

  private Env env;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() throws Exception {
    this.topicControllerService = new TopicControllerService(clusterApiService, mailService);
    this.topicSyncControllerService = new TopicSyncControllerService();
    utilMethods = new UtilMethods();
    this.env = new Env();
    env.setId("1");
    env.setName("DEV");
    ReflectionTestUtils.setField(topicControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(topicControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(topicControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        topicControllerService,
        "rolesPermissionsControllerService",
        rolesPermissionsControllerService);

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

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @Order(1)
  public void createTopicsSuccess() throws KlawException {
    HashMap<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");

    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.requestForTopic(any())).thenReturn(resultMap);
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    HashMap<String, String> result = topicControllerService.createTopicsRequest(getCorrectTopic());

    assertEquals("success", result.get("result"));
  }

  @Test
  @Order(2)
  public void createTopicsSuccess1() throws KlawException {
    HashMap<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");

    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.requestForTopic(any())).thenReturn(resultMap);
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    HashMap<String, String> result = topicControllerService.createTopicsRequest(getFailureTopic());

    assertEquals("success", result.get("result"));
  }

  // invalid partitions
  @Test
  @Order(3)
  public void createTopicsFailure() throws KlawException {
    HashMap<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "failure");

    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.requestForTopic(any())).thenReturn(resultMap);
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    HashMap<String, String> result = topicControllerService.createTopicsRequest(getFailureTopic1());

    assertEquals("failure", result.get("result"));
  }

  @Test
  @Order(4)
  public void createTopicsFailure1() throws KlawException {

    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvListsIncorrect1());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    HashMap<String, String> result = topicControllerService.createTopicsRequest(getFailureTopic1());

    assertNull(result.get("result"));
  }

  @Test
  @Order(7)
  public void updateSyncTopicsSuccess() {
    HashMap<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");

    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.addToSynctopics(any())).thenReturn("success");

    HashMap<String, String> result =
        topicSyncControllerService.updateSyncTopics(utilMethods.getSyncTopicUpdates());

    assertEquals("success", result.get("result"));
  }

  @Test
  @Order(8)
  public void updateSyncTopicsNoUpdate() {
    List<SyncTopicUpdates> topicUpdates = new ArrayList<>();

    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    HashMap<String, String> result = topicSyncControllerService.updateSyncTopics(topicUpdates);

    assertEquals("No record updated.", result.get("result"));
  }

  @Test
  @Order(9)
  public void updateSyncTopicsNotAuthorized() {
    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    HashMap<String, String> result =
        topicSyncControllerService.updateSyncTopics(utilMethods.getSyncTopicUpdates());

    assertEquals("Not Authorized.", result.get("result"));
  }

  @Test
  @Order(10)
  public void getCreatedTopicRequests1() {
    List<TopicRequest> listTopicReqs = new ArrayList<>();
    listTopicReqs.add(getCorrectTopicDao());
    listTopicReqs.add(getCorrectTopicDao());

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedTopicRequests(anyString(), anyString(), anyBoolean(), anyInt()))
        .thenReturn(listTopicReqs);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFTATEAM");

    List<TopicRequestModel> topicList =
        topicControllerService.getCreatedTopicRequests("1", "", "all");

    assertEquals(2, topicList.size());
  }

  @Test
  @Order(11)
  public void getCreatedTopicRequests2() {
    List<TopicRequest> listTopicReqs = new ArrayList<>();
    listTopicReqs.add(getTopicRequest("topic1"));
    listTopicReqs.add(getTopicRequest("topic2"));
    listTopicReqs.add(getTopicRequest("topic3"));
    listTopicReqs.add(getTopicRequest("topic4"));
    listTopicReqs.add(getTopicRequest("topic5"));

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedTopicRequests(anyString(), anyString(), anyBoolean(), anyInt()))
        .thenReturn(listTopicReqs);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFTATEAM");

    List<TopicRequestModel> topicList =
        topicControllerService.getCreatedTopicRequests("1", "", "all");

    assertEquals(topicList.size(), 5);
    assertEquals(topicList.get(0).getTopicpartitions(), 2);
    assertEquals(topicList.get(0).getTopicname(), "topic1");
    assertEquals(topicList.get(1).getTopicname(), "topic2");
  }

  @Test
  @Order(12)
  public void deleteTopicRequests() {
    when(handleDbRequests.deleteTopicRequest(anyInt(), anyInt())).thenReturn("success");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    String result = topicControllerService.deleteTopicRequests("1001");
    assertEquals("{\"result\":\"success\"}", result);
  }

  @Test
  @Order(13)
  public void approveTopicRequestsSuccess() throws KlawException {
    String topicName = "topic1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(handleDbRequests.updateTopicRequest(any(), anyString())).thenReturn("success");
    when(clusterApiService.approveTopicRequests(
            anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt()))
        .thenReturn(new ResponseEntity<>("success", HttpStatus.OK));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    String result = topicControllerService.approveTopicRequests(topicId + "");

    assertEquals("{\"result\":\"success\"}", result);
  }

  @Test
  @Order(14)
  public void approveTopicRequestsFailure1() throws KlawException {
    String topicName = "topic1", env = "1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(handleDbRequests.updateTopicRequest(any(), anyString())).thenReturn("success");
    when(clusterApiService.approveTopicRequests(
            anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt()))
        .thenReturn(new ResponseEntity<>("failure error", HttpStatus.OK));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    String result = topicControllerService.approveTopicRequests("" + topicId);

    assertEquals("{\"result\":\"failure error\"}", result);
  }

  @Test
  @Order(15)
  public void getAllTopics() {
    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(utilMethods.getTopics());

    List<String> result = topicControllerService.getAllTopics(false);
    assertEquals(1, result.size());
    assertEquals(result.get(0), "testtopic");
  }

  @Test
  @Order(16)
  public void getTopicsSuccess1() throws Exception {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(getSyncTopics("topic", 4));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt()))
        .thenReturn("INFRATEAM", "INFRATEAM", "INFRATEAM", "INFRATEAM");
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    List<List<TopicInfo>> topicsList =
        topicControllerService.getTopics(envSel, pageNo, "", topicNameSearch, null, null);

    assertEquals(2, topicsList.size());
  }

  // topicSearch does not exist in topic names
  @Test
  @Order(18)
  public void getTopicsSearchFailure() throws Exception {
    String envSel = "1", pageNo = "1", topicNameSearch = "demo";

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(handleDbRequests.getSyncTopics(envSel, null, 1)).thenReturn(getSyncTopics("topic", 4));

    List<List<TopicInfo>> topicsList =
        topicControllerService.getTopics(envSel, pageNo, "", topicNameSearch, null, null);

    assertNull(topicsList);
  }

  @Test
  @Order(19)
  public void getSyncTopics() throws Exception {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAllTopics(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterApiTopics("topic", 10));
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(rolesPermissionsControllerService.getApproverRoles(anyString(), anyInt()))
        .thenReturn(List.of("USER"));

    HashMap<String, Object> topicRequests =
        topicSyncControllerService.getSyncTopics(
            envSel, pageNo, "", topicNameSearch, "false", false);
    assertEquals(topicRequests.size(), 2);
  }

  @Test
  @Order(20)
  public void declineTopicRequests() throws KlawException {
    String topicName = "testtopic";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.declineTopicRequest(any(), anyString())).thenReturn("success");
    String result = topicControllerService.declineTopicRequests(topicId + "", "Reason");

    assertEquals("{\"result\":\"" + "success" + "\"}", result);
  }

  @Test
  @Order(21)
  public void getTopicRequests() {

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.getAllTopicRequests(anyString(), anyInt()))
        .thenReturn(getListTopicRequests());
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFRATEAM");

    List<TopicRequestModel> listTopicRqs = topicControllerService.getTopicRequests("1", "", "all");
    assertEquals(listTopicRqs.size(), 2);
  }

  @Test
  @Order(22)
  public void getTopicTeam() {
    String topicName = "testtopic";
    stubUserInfo();
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(Arrays.asList(getTopic(topicName)));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    List<Topic> topicTeam = topicControllerService.getTopicFromName(topicName, 1);
    assertEquals(topicTeam.get(0).getTeamId(), Integer.valueOf(1));
  }

  private TopicRequestModel getCorrectTopic() {

    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTopictype(TopicRequestTypes.Create.toString());
    return topicRequest;
  }

  private TopicRequestModel getFailureTopic() {

    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTopictype(TopicRequestTypes.Create.toString());
    return topicRequest;
  }

  private TopicRequest getCorrectTopicDao() {

    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTeamId(101);
    topicRequest.setTopicstatus("created");
    return topicRequest;
  }

  private TopicRequest getFailureTopicDao() {

    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(3);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    return topicRequest;
  }

  private TopicRequestModel getFailureTopic1() {

    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(-1);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTopictype(TopicRequestTypes.Create.toString());
    return topicRequest;
  }

  private TopicRequest getTopicRequest(String name) {

    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTopicname(name);
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setReplicationfactor("1");
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTeamId(101);
    topicRequest.setTopicstatus("created");
    topicRequest.setRequestor("kwuserb");
    topicRequest.setTopictype("Create");
    return topicRequest;
  }

  private List<TopicRequest> getListTopicRequests() {

    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTopicname("testtopic1");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setTeamId(101);
    topicRequest.setTopicstatus("created");
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));

    List<TopicRequest> listReqs = new ArrayList<>();
    listReqs.add(topicRequest);

    TopicRequest topicRequest1 = new TopicRequest();
    topicRequest1.setTopicname("testtopic12");
    topicRequest1.setEnvironment(env.getId());
    topicRequest1.setTopicpartitions(2);
    topicRequest1.setTeamId(101);
    topicRequest1.setTopicstatus("created");
    topicRequest1.setRequesttime(new Timestamp(System.currentTimeMillis()));

    listReqs.add(topicRequest1);

    return listReqs;
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

  private Topic getTopic(String topicName) {
    Topic t = new Topic();
    t.setTeamId(1);
    t.setTopicname(topicName);
    t.setTopicid(1);
    t.setTenantId(0);
    t.setEnvironment("1");

    return t;
  }

  private List<Topic> getSyncTopics(String topicPrefix, int size) {
    List<Topic> listTopics = new ArrayList<>();
    Topic t;

    for (int i = 0; i < size; i++) {
      t = new Topic();

      if (i % 2 == 0) t.setTeamId(1);
      else t.setTeamId(2);

      t.setTopicname(topicPrefix + i);
      t.setTopicid(i);
      t.setEnvironment("1");
      t.setTeamId(101);

      listTopics.add(t);
    }
    return listTopics;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
