package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncAclUpdates;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclSyncControllerServiceTest {

  private UtilMethods utilMethods;
  @Mock private UserDetails userDetails;
  @Mock private ClusterApiService clusterApiService;
  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private ManageDatabase manageDatabase;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private Map<Integer, KwClusters> clustersHashMap;
  @Mock private KwClusters kwClusters;
  @Mock private MailUtils mailService;
  @Mock private UserInfo userInfo;
  private AclSyncControllerService aclSyncControllerService;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.aclSyncControllerService = new AclSyncControllerService(clusterApiService, mailService);

    Env env = new Env();
    env.setName("DEV");
    env.setId("1");
    ReflectionTestUtils.setField(aclSyncControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(
        aclSyncControllerService, "commonUtilsService", commonUtilsService);
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
  public void updateSyncAcls() throws KlawException {
    stubUserInfo();
    when(handleDbRequests.addToSyncacls(anyList())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    ApiResponse resultResp =
        aclSyncControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void updateSyncAclsFailure1() throws KlawException {
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse resultResp =
        aclSyncControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(3)
  public void updateSyncAclsFailure2() {
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.addToSyncacls(anyList())).thenThrow(new RuntimeException("Error"));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    try {
      aclSyncControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    } catch (KlawException e) {
      assertThat(e.getMessage()).isEqualTo("Error");
    }
  }

  @Test
  @Order(4)
  public void updateSyncAclsFailure3() throws KlawException {
    List<SyncAclUpdates> updates = new ArrayList<>();
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    ApiResponse resultResp = aclSyncControllerService.updateSyncAcls(updates);
    assertThat(resultResp.getMessage()).isEqualTo("No record updated.");
  }

  @Test
  @Order(5)
  public void updateSyncAclsFailure4() {
    when(handleDbRequests.addToSyncacls(anyList())).thenThrow(new RuntimeException("Error"));
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    try {
      aclSyncControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    } catch (KlawException e) {
      assertThat(e.getMessage()).isEqualTo("Error");
    }
  }

  @Test
  @Order(6)
  public void getAclsSyncTrue1() throws KlawException {
    String envSelected = "1", pageNo = "1", topicNameSearch = "testtopic1";

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAcls(anyString(), any(), any(KafkaSupportedProtocol.class), anyInt()))
        .thenReturn(utilMethods.getClusterAcls());
    when(handleDbRequests.getAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(handleDbRequests.getSyncAcls(anyString(), anyInt())).thenReturn(getAclsSOT0());
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");

    List<AclInfo> aclList =
        aclSyncControllerService.getSyncAcls(envSelected, pageNo, "", topicNameSearch, "");

    assertThat(aclList).isEmpty();
  }

  @Test
  @Order(7)
  public void getAclsSyncTrue2() throws KlawException {
    String envSelected = "1", pageNo = "1", topicNameSearch = "test";

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAcls(anyString(), any(), any(KafkaSupportedProtocol.class), anyInt()))
        .thenReturn(utilMethods.getClusterAcls());
    when(handleDbRequests.getAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(handleDbRequests.getSyncAcls(anyString(), anyInt())).thenReturn(getAclsSOT0());
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");

    List<AclInfo> aclList =
        aclSyncControllerService.getSyncAcls(envSelected, pageNo, "", topicNameSearch, "");

    assertThat(aclList).isEmpty();
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

  private List<Acl> getAclsSOT0() {
    List<Acl> aclList = new ArrayList();

    Acl aclReq = new Acl();
    aclReq.setReq_no(1001);
    aclReq.setTopicname("testtopic1");
    aclReq.setTeamId(1);
    aclReq.setAclip("2.1.2.1");
    aclReq.setAclssl(null);
    aclReq.setConsumergroup("mygrp1");
    aclReq.setAclType(AclType.CONSUMER.value);

    aclList.add(aclReq);

    return aclList;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
