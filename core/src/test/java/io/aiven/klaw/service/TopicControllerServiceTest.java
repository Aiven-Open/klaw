package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.TopicConfigEntry;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
  public void createTopicsSuccessAdvancedTopicConfigs()
      throws KlawException, KlawNotAuthorizedException {
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

    ApiResponse apiResponse =
        topicControllerService.createTopicsCreateRequest(getTopicWithAdvancedConfigs());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void createTopicsSuccessDefaultValues() throws KlawException, KlawNotAuthorizedException {
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

    ApiResponse apiResponse =
        topicControllerService.createTopicsCreateRequest(getTopicWithDefaultConfigs());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  // invalid partitions
  @Test
  @Order(3)
  public void createTopicsFailureInvalidPartitions()
      throws KlawException, KlawNotAuthorizedException {
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

    ApiResponse apiResponse = topicControllerService.createTopicsCreateRequest(getFailureTopic1());
    assertThat(apiResponse.getResult()).contains("failure");
  }

  @Test
  @Order(4)
  public void createTopicsFailureInvalidClusterTenantIds()
      throws KlawException, KlawNotAuthorizedException {

    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvListsIncorrect1());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    ApiResponse apiResponse = topicControllerService.createTopicsCreateRequest(getFailureTopic1());
    assertThat(apiResponse.getResult()).isEqualTo(null);
  }

  @Test
  @Order(5)
  public void createTopicDeleteRequestFailureNotAuthorized() throws KlawNotAuthorizedException {
    String topicName = "testtopic1";
    String envId = "1";
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(true);

    assertThrows(
        KlawNotAuthorizedException.class,
        () -> {
          topicControllerService.createTopicDeleteRequest(topicName, envId);
        });
  }

  @Test
  @Order(6)
  public void createTopicDeleteRequestFailureTopicAlreadyExists() {
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
    } catch (KlawNotAuthorizedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(7)
  public void createTopicDeleteRequestFailureNotOwnerTeamOfTopic() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo(
              "Failure. Sorry, you cannot delete this topic, as you are not part of this team.");
    } catch (KlawException e) {
      throw new RuntimeException(e);
    } catch (KlawNotAuthorizedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(8)
  public void createTopicDeleteRequestFailureTopicWithSubscriptions() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(commonUtilsService.getTeamId(anyString())).thenReturn(1);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getAcls());
    try {
      ApiResponse apiResponse = topicControllerService.createTopicDeleteRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo(
              "Failure. There are existing subscriptions for topic. Please get them deleted before.");
    } catch (KlawException e) {
      throw new RuntimeException(e);
    } catch (KlawNotAuthorizedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(9)
  public void createTopicDeleteRequestFailureTopicNotInCluster() {
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
    } catch (KlawNotAuthorizedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(10)
  public void createTopicDeleteRequestSuccessDefaultValues() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(commonUtilsService.getTeamId(anyString())).thenReturn(1);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));
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
    } catch (KlawNotAuthorizedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(11)
  public void createClaimTopicRequestFailureRequestAlreadyExists() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(getListTopicRequests());
    try {
      ApiResponse apiResponse = topicControllerService.createClaimTopicRequest(topicName, envId);
      assertThat(apiResponse.getResult())
          .isEqualTo("Failure. A request already exists for this topic.");
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(12)
  public void createClaimTopicRequestSuccessDefaultValues() {
    String topicName = "testtopic1";
    String envId = "1";
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));
    List<UserInfo> userList = utilMethods.getUserInfoList("testuser", "");
    userList.get(0).setTeamId(1);
    when(handleDbRequests.selectAllUsersInfo(anyInt())).thenReturn(userList);
    Map<String, String> claimReqResult = new HashMap<>();
    claimReqResult.put("result", ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.requestForTopic(any())).thenReturn(claimReqResult);
    try {
      ApiResponse apiResponse = topicControllerService.createClaimTopicRequest(topicName, envId);
      assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(13)
  public void getTopicRequests() {
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getAllTopicRequests(
            anyString(), anyString(), eq(null), eq(false), anyInt()))
        .thenReturn(getListTopicRequests());
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFRATEAM");

    List<TopicRequestModel> listTopicRqs =
        topicControllerService.getTopicRequests("1", "", "all", null, false);
    assertThat(listTopicRqs).hasSize(2);
  }

  @Test
  @Order(14)
  public void getTopicRequestsClaimType() {
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<TopicRequest> topicRequests = getListTopicRequests();
    topicRequests.get(0).setRequestOperationType(RequestOperationType.CLAIM.value);
    when(handleDbRequests.getAllTopicRequests(
            anyString(), anyString(), eq(null), eq(false), anyInt()))
        .thenReturn(topicRequests);
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFRATEAM");
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());

    List<TopicRequestModel> listTopicRqs =
        topicControllerService.getTopicRequests("1", "", "all", null, false);
    assertThat(listTopicRqs).hasSize(2);
  }

  @Test
  @Order(15)
  public void getTopicRequestsOtherType() {
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    List<TopicRequest> topicRequests = getListTopicRequests();
    topicRequests.get(0).setRequestOperationType(RequestOperationType.CLAIM.value);
    when(handleDbRequests.getAllTopicRequests(
            anyString(), anyString(), eq(null), eq(false), anyInt()))
        .thenReturn(topicRequests);
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFRATEAM");
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());

    List<TopicRequestModel> listTopicRqs =
        topicControllerService.getTopicRequests("1", "", "created", null, false);
    assertThat(listTopicRqs).hasSize(2);
  }

  @Test
  @Order(16)
  public void getTopicTeamOnlyFailureNoTopicsFound() {
    String topicName = "testtopic";
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    Map<String, String> topicTeamMap =
        topicControllerService.getTopicTeamOnly(topicName, AclPatternType.PREFIXED);
    assertThat(topicTeamMap.get("error")).contains("There are no topics found with this prefix.");
  }

  @Test
  @Order(17)
  public void getTopicTeamOnlyFailurePrefixInMultipleTeams() {
    String topicName = "testtopic";
    List<Topic> topicList = utilMethods.getTopics();
    Topic topic = new Topic();
    topic.setTopicname("testtopic2");
    topic.setEnvironment("1");
    topicList.add(topic);

    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getAllTopics(anyInt())).thenReturn(topicList);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    Map<String, String> topicTeamMap =
        topicControllerService.getTopicTeamOnly(topicName, AclPatternType.PREFIXED);
    assertThat(topicTeamMap.get("error"))
        .contains("There are atleast two topics with same prefix owned by different teams.");
  }

  @Test
  @Order(18)
  public void getTopicTeamOnlySuccessPatternPrefixed() {
    String topicName = "testtopic";
    String teamName = "TestTeam";
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getAllTopics(anyInt())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn(teamName);
    Map<String, String> topicTeamMap =
        topicControllerService.getTopicTeamOnly(topicName, AclPatternType.PREFIXED);
    assertThat(topicTeamMap.get("team")).isEqualTo(teamName);
  }

  @Test
  @Order(19)
  public void getTopicTeamOnlySuccessPatternLiteral() {
    String topicName = "testtopic";
    String teamName = "TestTeam";
    stubUserInfo();
    List<Topic> topicList = utilMethods.getTopics();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(topicList);
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topicList);
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn(teamName);
    Map<String, String> topicTeamMap =
        topicControllerService.getTopicTeamOnly(topicName, AclPatternType.LITERAL);
    assertThat(topicTeamMap.get("team")).isEqualTo(teamName);
  }

  @Test
  @Order(20)
  public void getCreatedTopicRequests() {
    List<TopicRequest> listTopicReqs = new ArrayList<>();
    listTopicReqs.add(getCorrectTopicDao());
    listTopicReqs.add(getCorrectTopicDao());

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedTopicRequests(
            anyString(), anyString(), anyBoolean(), anyInt(), eq(null), eq(null), eq(null)))
        .thenReturn(listTopicReqs);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFTATEAM");

    List<TopicRequestModel> topicList =
        topicControllerService.getTopicRequestsForApprover("1", "", "all", null, null, null);

    assertThat(topicList).hasSize(2);
  }

  @Test
  @Order(21)
  public void getCreatedTopicRequestsWithMoreElements() {
    List<TopicRequest> listTopicReqs = new ArrayList<>();
    listTopicReqs.add(getTopicRequest("topic1"));
    listTopicReqs.add(getTopicRequest("topic2"));
    listTopicReqs.add(getTopicRequest("topic3"));
    listTopicReqs.add(getTopicRequest("topic4"));
    listTopicReqs.add(getTopicRequest("topic5"));

    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedTopicRequests(
            anyString(), anyString(), anyBoolean(), anyInt(), eq(null), eq(null), eq(null)))
        .thenReturn(listTopicReqs);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFTATEAM");

    List<TopicRequestModel> topicList =
        topicControllerService.getTopicRequestsForApprover("1", "", "all", null, null, null);

    assertThat(topicList).hasSize(5);
    assertThat(topicList.get(0).getTopicpartitions()).isEqualTo(2);
    assertThat(topicList.get(0).getTopicname()).isEqualTo("topic1");
    assertThat(topicList.get(1).getTopicname()).isEqualTo("topic2");
  }

  @Test
  @Order(22)
  public void deleteTopicRequests() throws KlawException {
    when(handleDbRequests.deleteTopicRequest(anyInt(), anyString(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(mailService.getUserName(any())).thenReturn("uiuser1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    ApiResponse resultResp = topicControllerService.deleteTopicRequests("1001");
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(23)
  public void approveTopicRequests() throws KlawException {
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
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests(topicId + "");
    assertThat(apiResponse1.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(24)
  public void approveTopicClaimRequests() throws KlawException {
    String topicName = "topic1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);
    topicRequest.setRequestOperationType(RequestOperationType.CLAIM.value);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(handleDbRequests.updateTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(clusterApiService.approveTopicRequests(
            anyString(), anyString(), anyInt(), anyString(), anyString(), any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));
    when(handleDbRequests.addToSynctopics(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.updateTopicRequestStatus(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests(topicId + "");
    assertThat(apiResponse1.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(25)
  public void approveTopicUpdateRequests() throws KlawException {
    String topicName = "topic1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);
    topicRequest.setRequestOperationType(RequestOperationType.UPDATE.value);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(handleDbRequests.updateTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(clusterApiService.approveTopicRequests(
            anyString(), anyString(), anyInt(), anyString(), anyString(), any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("INFRATEAM");
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests(topicId + "");
    assertThat(apiResponse1.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(26)
  public void approveTopicRequestsFailureResponseFromCluster() throws KlawException {
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
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests("" + topicId);
    assertThat(apiResponse1.getResult()).isEqualTo("failure");
  }

  @Test
  @Order(27)
  public void approveTopicRequestsFailureNotAllowed() throws KlawException {
    String topicName = "topic1";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);
    topicRequest.setRequestor("kwusera");

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);

    ApiResponse apiResponse1 = topicControllerService.approveTopicRequests("" + topicId);
    assertThat(apiResponse1.getResult())
        .isEqualTo("You are not allowed to approve your own topic requests.");
  }

  @Test
  @Order(28)
  public void getAllTopics() {
    stubUserInfo();

    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(utilMethods.getTopics());

    List<String> result = topicControllerService.getAllTopics(false, "DEV");
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo("testtopic");
  }

  @Test
  @Order(29)
  public void getAllTopicsForMyTeam() {
    stubUserInfo();

    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getTeamId(anyString())).thenReturn(3);

    List<String> result = topicControllerService.getAllTopics(true, "DEV");
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo("testtopic");
  }

  @Test
  @Order(30)
  public void saveTopicDocumentation() throws KlawException {
    stubUserInfo();
    TopicInfo topicInfo = utilMethods.getTopicInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic("testtopic")));
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getTeamId(anyString())).thenReturn(3);

    when(handleDbRequests.updateTopicDocumentation(any()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse apiResponse = topicControllerService.saveTopicDocumentation(topicInfo);
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(31)
  public void saveTopicDocumentationFailureInDbUpdate() throws KlawException {
    stubUserInfo();
    TopicInfo topicInfo = utilMethods.getTopicInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic("testtopic")));
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getTeamId(anyString())).thenReturn(1);

    when(handleDbRequests.updateTopicDocumentation(any()))
        .thenReturn(ApiResultStatus.FAILURE.value);

    ApiResponse apiResponse = topicControllerService.saveTopicDocumentation(topicInfo);
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.FAILURE.value);
  }

  @Test
  @Order(32)
  public void getTopicDetailsPerEnvFailureTopicDoesNotExist() {
    stubUserInfo();
    String envId = "1", topicName = "testtopic";
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(Collections.emptyList());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    Map<String, Object> topicDetailsPerEnvResponse =
        topicControllerService.getTopicDetailsPerEnv(envId, topicName);
    assertThat(topicDetailsPerEnvResponse.get("error")).isEqualTo("Topic does not exist.");
  }

  @Test
  @Order(33)
  public void getTopicDetailsPerEnvFailureNotOwnerTeamOfTopic() {
    stubUserInfo();
    String envId = "1", topicName = "testtopic";
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());

    Map<String, Object> topicDetailsPerEnvResponse =
        topicControllerService.getTopicDetailsPerEnv(envId, topicName);
    assertThat(topicDetailsPerEnvResponse.get("error"))
        .isEqualTo("Sorry, your team does not own the topic !!");
  }

  @Test
  @Order(34)
  public void getTopicDetailsPerEnv() {
    stubUserInfo();
    String envId = "1", topicName = "testtopic";
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(utilMethods.getTopics());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getTeamId(anyString())).thenReturn(3);

    Map<String, Object> topicDetailsPerEnvResponse =
        topicControllerService.getTopicDetailsPerEnv(envId, topicName);
    assertThat(topicDetailsPerEnvResponse.get("topicExists")).isEqualTo(true);
    assertThat(topicDetailsPerEnvResponse.get("topicContents")).isNotNull();
  }

  @Test
  @Order(35)
  public void getTopics() {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(getSyncTopics("topic", 4));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt()))
        .thenReturn(
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM);
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");
    when(commonUtilsService.groupTopicsByEnv(any())).thenReturn(getSyncTopics("topic", 4));

    List<List<TopicInfo>> topicsList =
        topicControllerService.getTopics(envSel, pageNo, "", topicNameSearch, null, null);

    assertThat(topicsList).hasSize(2);
  }

  @Test
  @Order(36)
  public void getTopicsWithProducerFilter() {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(getSyncTopics("topic", 4));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt()))
        .thenReturn(
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM);
    when(handleDbRequests.selectAllTopicsByTopictypeAndTeamname(anyString(), anyInt(), anyInt()))
        .thenReturn(getSyncTopics("topic", 4));
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");
    when(commonUtilsService.groupTopicsByEnv(any())).thenReturn(getSyncTopics("topic", 4));
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(utilMethods.getTopics());

    List<List<TopicInfo>> topicsList =
        topicControllerService.getTopics(
            envSel, pageNo, "", topicNameSearch, KwConstants.INFRATEAM, AclType.PRODUCER.value);

    assertThat(topicsList).isNull(); // for this filter criteria, there are no topics.
  }

  @Test
  @Order(37)
  public void getTopicsWithPatternFilter() {
    String envSel = "1", pageNo = "1", topicNameSearch = "top";

    stubUserInfo();
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(handleDbRequests.getSyncTopics(any(), any(), anyInt()))
        .thenReturn(getSyncTopics("topic", 4));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt()))
        .thenReturn(
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM,
            KwConstants.INFRATEAM);
    List<Topic> syncTopics = getSyncTopics("topic", 4);
    syncTopics.get(0).setEnvironmentsList(List.of("1", "2"));
    syncTopics.get(0).setTopicname("testtopic");
    when(handleDbRequests.selectAllTopicsByTopictypeAndTeamname(anyString(), anyInt(), anyInt()))
        .thenReturn(getSyncTopics("topic", 4));
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");
    when(commonUtilsService.groupTopicsByEnv(any())).thenReturn(getSyncTopics("topic", 4));
    List<Topic> topicList = utilMethods.getTopics();
    topicList.get(0).setTopicname("testtopic" + "--" + AclPatternType.PREFIXED + "--");
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(topicList);

    List<List<TopicInfo>> topicsList =
        topicControllerService.getTopics(
            envSel, pageNo, "", topicNameSearch, KwConstants.INFRATEAM, AclType.PRODUCER.value);

    assertThat(topicsList).isNull(); // for this filter criteria, there are no topics.
  }

  // topicSearch does not exist in topic names
  @Test
  @Order(38)
  public void getTopicsSearchFailureNotExistingSearch() {
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
  @Order(39)
  public void declineTopicRequests() throws KlawException {
    String topicName = "testtopic";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.declineTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse resultResp = topicControllerService.declineTopicRequests(topicId + "", "Reason");

    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(40)
  public void declineTopicRequestsFailureRequestDoesNotExist() throws KlawException {
    String topicName = "testtopic";
    int topicId = 1001;
    TopicRequest topicRequest = getTopicRequest(topicName);
    topicRequest.setRequestStatus(RequestStatus.APPROVED.value);

    stubUserInfo();
    when(handleDbRequests.selectTopicRequestsForTopic(anyInt(), anyInt())).thenReturn(topicRequest);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.declineTopicRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse resultResp = topicControllerService.declineTopicRequests(topicId + "", "Reason");

    assertThat(resultResp.getResult()).isEqualTo("This request does not exist anymore.");
  }

  @Test
  @Order(41)
  public void getTopicTeam() {
    String topicName = "testtopic";
    stubUserInfo();
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(List.of(getTopic(topicName)));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(getTopic(topicName)));

    List<Topic> topicTeam = topicControllerService.getTopicFromName(topicName, 1);
    assertThat(topicTeam.get(0).getTeamId()).isOne();
  }

  @Test
  @Order(42)
  public void getTopicEvents() throws KlawException {
    String envId = "1", consumerGroupId = "consuemrgroup", topicName = "testtopic", offsetId = "5";
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);

    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    kwClustersMap.put(1, utilMethods.getKwClusters());
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    Map<String, String> eventsMap = new HashMap<>();
    eventsMap.put("1", "hello world1"); // offsetid, content
    eventsMap.put("2", "hello world2"); // offsetid, content
    when(clusterApiService.getTopicEvents(
            anyString(), any(), anyString(), anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(eventsMap);

    Map<String, String> topicEventsMap =
        topicControllerService.getTopicEvents(envId, consumerGroupId, topicName, offsetId);
    assertThat(topicEventsMap).hasSize(2);
  }

  @Test
  @Order(43)
  public void getExistingTopicRequests() {
    when(handleDbRequests.selectTopicRequests(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getTopicRequests());
    List<TopicRequest> topicReqResponse =
        topicControllerService.getExistingTopicRequests(
            utilMethods.getTopicCreateRequestModel(101), 1);
    assertThat(topicReqResponse).hasSize(1);
  }

  @Test
  @Order(44)
  public void updateTopicsSuccessAdvancedTopicConfigs()
      throws KlawException, KlawNotAuthorizedException {
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

    ApiResponse apiResponse =
        topicControllerService.createTopicsUpdateRequest(getTopicWithAdvancedConfigs());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(45)
  public void updateTopicsSuccessDefaultValues() throws KlawException, KlawNotAuthorizedException {
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

    ApiResponse apiResponse =
        topicControllerService.createTopicsUpdateRequest(getTopicWithDefaultConfigs());
    assertThat(apiResponse.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  // invalid partitions
  @Test
  @Order(46)
  public void updateTopicsFailureInvalidPartitions()
      throws KlawException, KlawNotAuthorizedException {
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

    ApiResponse apiResponse = topicControllerService.createTopicsUpdateRequest(getFailureTopic1());
    assertThat(apiResponse.getResult()).contains("failure");
  }

  @Test
  @Order(47)
  public void updateTopicsFailureInvalidClusterTenantIds()
      throws KlawException, KlawNotAuthorizedException {

    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvListsIncorrect1());
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1");

    ApiResponse apiResponse = topicControllerService.createTopicsUpdateRequest(getFailureTopic1());
    assertThat(apiResponse.getResult()).isEqualTo(null);
  }

  @Test
  @Order(48)
  public void updateTopicsFailureNotAuthorized() {

    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.REQUEST_EDIT_TOPICS)))
        .thenReturn(true);
    assertThrows(
        KlawNotAuthorizedException.class,
        () -> {
          topicControllerService.createTopicsUpdateRequest(getFailureTopic1());
        });
  }

  private TopicRequestModel getTopicWithAdvancedConfigs() {
    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setRequestOperationType(RequestOperationType.CREATE);
    List<TopicConfigEntry> topicConfigEntryList = new ArrayList<>();
    TopicConfigEntry topicConfigEntry1 = new TopicConfigEntry("compression.type", "snappy");
    TopicConfigEntry topicConfigEntry2 = new TopicConfigEntry("flush.ms", "12345");
    topicConfigEntryList.add(topicConfigEntry1);
    topicConfigEntryList.add(topicConfigEntry2);
    topicRequest.setAdvancedTopicConfigEntries(topicConfigEntryList);
    return topicRequest;
  }

  private TopicRequestModel getTopicWithDefaultConfigs() {
    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setRequestOperationType(RequestOperationType.CREATE);
    return topicRequest;
  }

  private TopicRequest getCorrectTopicDao() {
    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setTeamId(101);
    topicRequest.setRequestor("Jackie");
    topicRequest.setRequestStatus("created");
    return topicRequest;
  }

  private TopicRequestModel getFailureTopic1() {
    TopicRequestModel topicRequest = new TopicRequestModel();
    topicRequest.setTopicname("newtopicname");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(-1);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setRequestOperationType(RequestOperationType.CREATE);
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
    topicRequest.setRequestStatus("created");
    topicRequest.setRequestor("kwuserb");
    topicRequest.setJsonParams(
        "{\"advancedTopicConfiguration\":{\"compression.type\":\"snappy\",\"cleanup.policy\":\"compact\"}}");
    topicRequest.setRequestOperationType(RequestOperationType.CREATE.value);
    topicRequest.setTenantId(101);
    return topicRequest;
  }

  private List<TopicRequest> getListTopicRequests() {
    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTopicname("testtopic1");
    topicRequest.setEnvironment(env.getId());
    topicRequest.setTopicpartitions(2);
    topicRequest.setTeamId(101);
    topicRequest.setRequestStatus("created");
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));

    List<TopicRequest> listReqs = new ArrayList<>();
    listReqs.add(topicRequest);

    TopicRequest topicRequest1 = new TopicRequest();
    topicRequest1.setTopicname("testtopic12");
    topicRequest1.setEnvironment(env.getId());
    topicRequest1.setTopicpartitions(2);
    topicRequest1.setTeamId(101);
    topicRequest1.setRequestStatus("created");
    topicRequest1.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest1.setJsonParams(
        "{\"advancedTopicConfiguration\":{\"compression.type\":\"snappy\",\"cleanup.policy\":\"compact\"}}");

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
    t.setHistory(
        "[{\"environmentName\":\"DEV\",\"teamName\":\"Team\",\"requestedBy\":\"user\","
            + "\"requestedTime\":\"2022-Sep-23 13:38:22\",\"approvedBy\":\"user\","
            + "\"approvedTime\":\"2022-Sep-23 13:38:52\",\"remarks\":\"Create\"}]");

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
      t.setEnvironmentsList(new ArrayList<>());

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
