package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KafkaSupportedProtocol;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.SyncTopicUpdates;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicSyncControllerServiceTest {

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

  @BeforeEach
  public void setUp() throws Exception {
    this.topicSyncControllerService = new TopicSyncControllerService();
    utilMethods = new UtilMethods();
    Env env = new Env();
    env.setId("1");
    env.setName("DEV");

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
  public void updateSyncTopicsSuccess() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(Collections.singletonList("1"));
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
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
