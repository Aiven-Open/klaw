package io.aiven.klaw.service;

import static io.aiven.klaw.model.ApiResultStatus.NOT_AUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.RequestOperationType;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.TopicRequestTypes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

  @Mock private ManageDatabase manageDatabase;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock CommonUtilsService commonUtilsService;

  @Mock private MailUtils mailService;

  private TopicControllerService topicControllerService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  @Mock Map<Integer, KwTenantConfigModel> tenantConfig;

  @Mock KwTenantConfigModel tenantConfigModel;

  private Env env;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() throws Exception {
    this.topicControllerService = new TopicControllerService(clusterApiService, mailService);
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
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", ApiResultStatus.SUCCESS.value);

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

    ApiResponse apiResponse = topicControllerService.createTopicsRequest(getCorrectTopic());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void createTopicsSuccess1() throws KlawException {
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", ApiResultStatus.SUCCESS.value);

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

    ApiResponse apiResponse = topicControllerService.createTopicsRequest(getFailureTopic());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  // invalid partitions
  @Test
  @Order(3)
  public void createTopicsFailure() throws KlawException {
    Map<String, String> resultMap = new HashMap<>();
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

    ApiResponse apiResponse = topicControllerService.createTopicsRequest(getFailureTopic1());
    assertThat(apiResponse.getResult()).contains("failure");
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

    ApiResponse apiResponse = topicControllerService.createTopicsRequest(getFailureTopic1());
    assertThat(apiResponse.getResult()).isEqualTo(null);
  }

  @Test
  @Order(5)
  public void createTopicDeleteRequestFailure1() {
    String topicName = "testtopic1";
    String envId = "1";
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(true);

    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult()).isEqualTo(NOT_AUTHORIZED.value);
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(6)
  public void createTopicDeleteRequestFailure2() {
    String topicName = "testtopic1";
    String envId = "1";
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(getListTopicRequests());
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo("Failure. A delete topic request already exists.");
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(7)
  public void createTopicDeleteRequestFailure3() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo("Failure. You cannot delete this topic, as you are not part of this team.");
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(8)
  public void createTopicDeleteRequestFailure4() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(userInfo.getTeamId()).thenReturn(1);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getAcls());
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo(
              "Failure. There are existing subscriptions for topic. Please get them deleted before.");
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(9)
  public void createTopicDeleteRequestFailure5() {
    String topicName = "testtopic1";
    String envId = "2";
    stubUserInfo();
    when(userInfo.getTeamId()).thenReturn(1);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo("Failure. Topic not found on cluster: " + topicName);
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(10)
  public void createTopicDeleteRequestSuccess() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(userInfo.getTeamId()).thenReturn(1);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    Map<String, String> deleteReqResult = new HashMap<>();
    deleteReqResult.put("result", ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.requestForTopic(any())).thenReturn(deleteReqResult);
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(11)
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

    assertThat(topicList).hasSize(2);
  }

  @Test
  @Order(12)
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

    assertThat(topicList).hasSize(5);
    assertThat(topicList.get(0).getTopicpartitions()).isEqualTo(2);
    assertThat(topicList.get(0).getTopicname()).isEqualTo("topic1");
    assertThat(topicList.get(1).getTopicname()).isEqualTo("topic2");
  }

  @Test
  @Order(13)
  public void deleteTopicRequests() throws KlawException {
    when(handleDbRequests.deleteTopicRequest(anyInt(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    ApiResponse resultResp = topicControllerService.deleteTopicRequests("1001");
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(14)
  public void approveTopicRequestsSuccess() throws KlawException {
    String topicName = "topic1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(handleDbRequests.updateTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(clusterApiService.approveTopicRequests(
            anyString(), anyString(), anyInt(), anyString(), anyString(), any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests(topicId + "");
    assertThat(apiResponse1.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(15)
  public void approveTopicRequestsFailure1() throws KlawException {
    String topicName = "topic1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(handleDbRequests.updateTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(clusterApiService.approveTopicRequests(
            anyString(), anyString(), anyInt(), anyString(), anyString(), any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests("" + topicId);
    assertThat(apiResponse.getResult()).isEqualTo("failure");
  }

  @Test
  @Order(16)
  public void getAllTopics() {
    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(utilMethods.getTopics());

    List<String> result = topicControllerService.getAllTopics(false);
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo("testtopic");
  }

  @Test
  @Order(17)
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

    assertThat(topicsList).hasSize(2);
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

    assertThat(topicsList).isNull();
  }

  @Test
  @Order(19)
  public void declineTopicRequests() throws KlawException {
    String topicName = "testtopic";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.declineTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse resultResp = topicControllerService.declineTopicRequests(topicId + "", "Reason");

    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(20)
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
    assertThat(listTopicRqs).hasSize(2);
  }

  @Test
  @Order(21)
  public void getTopicTeam() {
    String topicName = "testtopic";
    stubUserInfo();
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    List<Topic> topicTeam = topicControllerService.getTopicFromName(topicName, 1);
    assertThat(topicTeam.get(0).getTeamId()).isOne();
  }

  private TopicRequestModel getCorrectTopic() {
    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTopictype(TopicRequestTypes.Create.toString());
    topicRequest.setAdvancedTopicConfigEntries(null);
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
    topicRequest.setTopictype(RequestOperationType.CREATE.value);
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
