package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.AclPatternType;
import io.aiven.klaw.model.AclRequestsModel;
import io.aiven.klaw.model.AclType;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.SyncAclUpdates;
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
public class AclControllerServiceTest {

  private UtilMethods utilMethods;

  @Mock private UserDetails userDetails;

  @Mock private ClusterApiService clusterApiService;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock private ManageDatabase manageDatabase;

  @Mock private CommonUtilsService commonUtilsService;

  @Mock private RolesPermissionsControllerService rolesPermissionsControllerService;

  @Mock private Map<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

  @Mock private MailUtils mailService;

  @Mock private UserInfo userInfo;

  private AclControllerService aclControllerService;

  private AclSyncControllerService aclSyncControllerService;
  private Env env;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.aclControllerService = new AclControllerService(clusterApiService, mailService);
    this.aclSyncControllerService = new AclSyncControllerService(clusterApiService, mailService);

    this.env = new Env();
    env.setName("DEV");
    env.setId("1");
    ReflectionTestUtils.setField(aclControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(aclControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(aclSyncControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(
        aclSyncControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        aclControllerService,
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
  public void createAcl() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequest();
    copyProperties(aclRequests, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);
    stubUserInfo();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequests);
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void updateSyncAcls() throws KlawException {
    stubUserInfo();
    when(handleDbRequests.addToSyncacls(anyList())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse resultResp =
        aclSyncControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(3)
  public void updateSyncAclsFailure1() throws KlawException {
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse resultResp =
        aclSyncControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(4)
  public void updateSyncAclsFailure2() throws KlawException {
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

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }

  @Test
  @Order(5)
  public void updateSyncAclsFailure3() throws KlawException {
    List<SyncAclUpdates> updates = new ArrayList<>();
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    ApiResponse resultResp = aclSyncControllerService.updateSyncAcls(updates);
    assertThat(resultResp.getResult()).isEqualTo("No record updated.");
  }

  @Test
  @Order(6)
  public void updateSyncAclsFailure4() throws KlawException {
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
  @Order(7)
  public void getAclRequests() {
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getAllAclRequests(
            anyBoolean(), anyString(), anyString(), anyString(), anyBoolean(), anyInt()))
        .thenReturn(getAclRequests("testtopic", 5));
    when(rolesPermissionsControllerService.getApproverRoles(anyString(), anyInt()))
        .thenReturn(Collections.singletonList("USER"));
    List<AclRequestsModel> aclReqs = aclControllerService.getAclRequests("1", "", "all");
    assertThat(aclReqs).isEmpty();
  }

  @Test
  @Order(8)
  public void getCreatedAclRequests() {
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedAclRequestsByStatus(
            anyString(), anyString(), anyBoolean(), anyInt()))
        .thenReturn(getAclRequests("testtopic", 16));
    List<AclRequestsModel> listReqs = aclControllerService.getCreatedAclRequests("", "", "");

    assertThat(listReqs).isEmpty();
  }

  @Test
  @Order(9)
  public void deleteAclRequests() throws KlawException {
    String req_no = "1001";
    when(handleDbRequests.deleteAclRequest(Integer.parseInt(req_no), 1))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse result = aclControllerService.deleteAclRequests(req_no);
    // assertThat(result).isEqualTo("{\"result\":\"null\"}");
  }

  @Test
  @Order(10)
  public void deleteAclRequestsFailure() throws KlawException {
    String req_no = "1001";
    when(handleDbRequests.deleteAclRequest(Integer.parseInt(req_no), 1)).thenReturn("failure");
    ApiResponse result = aclControllerService.deleteAclRequests(req_no);
    // assertThat(result).isEqualTo("{\"result\":\"null\"}");
  }

  @Test
  @Order(11)
  public void approveAclRequests() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);

    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse apiResp = aclControllerService.approveAclRequests("112");
    assertThat(apiResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(12)
  public void approveAclRequestsFailure1() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();
    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);

    ApiResponse apiResponse = ApiResponse.builder().result("failure").build();
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getResult()).isEqualTo("failure");
  }

  @Test
  @Order(13)
  public void approveAclRequestsFailure2() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyString()))
        .thenThrow(new RuntimeException("Error"));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getResult()).isEqualTo("failure");
  }

  @Test
  @Order(14)
  public void approveAclRequestsFailure3() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();
    aclReq.setAclstatus("completed");

    stubUserInfo();
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getResult()).isEqualTo("This request does not exist anymore.");
  }

  @Test
  @Order(15)
  public void declineAclRequests() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(handleDbRequests.declineAclRequest(any(), any()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse resultResp = aclControllerService.declineAclRequests(req_no, "");
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(16)
  public void declineAclRequestsFailure() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse result = aclControllerService.declineAclRequests(req_no, "Reason");

    // assertThat(result).isEqualTo("{\"result\":\"null\"}");
  }

  @Test
  @Order(17)
  public void getAclsSyncFalse1() throws KlawException {
    String env1 = "1", topicNameSearch = "testtopic";

    String RETRIEVE_SCHEMAS_KEY = "klaw.getschemas.enable";
    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(manageDatabase.getKwPropertyValue(anyString(), anyInt())).thenReturn("true");
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(utilMethods.getTeams());
    when(handleDbRequests.getTopics(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(getAclsSOT(topicNameSearch));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));

    List<AclInfo> aclList = aclControllerService.getAcls(topicNameSearch).getAclInfoList();

    assertThat(aclList).hasSize(1);

    assertThat(aclList.get(0).getTopicname()).isEqualTo(topicNameSearch);
    assertThat(aclList.get(0).getConsumergroup()).isEqualTo("mygrp1");
    assertThat(aclList.get(0).getAcl_ip()).isEqualTo("2.1.2.1");
  }

  @Test
  @Order(18)
  public void getAclsSyncFalse2() throws KlawException {
    String env1 = "1", topicNameSearch = "testnewtopic1";

    String RETRIEVE_SCHEMAS_KEY = "klaw.getschemas.enable";

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(manageDatabase.getKwPropertyValue(anyString(), anyInt())).thenReturn("true");
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(utilMethods.getTeams());
    when(handleDbRequests.getTopics(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(getAclsSOT(topicNameSearch));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));

    List<AclInfo> aclList = aclControllerService.getAcls(topicNameSearch).getAclInfoList();

    assertThat(aclList).hasSize(1);

    assertThat(aclList.get(0).getTopicname()).isEqualTo(topicNameSearch);
    assertThat(aclList.get(0).getConsumergroup()).isEqualTo("mygrp1");
    assertThat(aclList.get(0).getAcl_ip()).isEqualTo("2.1.2.1");
  }

  @Test
  @Order(19)
  public void getAclsSyncTrue1() throws KlawException {
    String envSelected = "1", pageNo = "1", topicNameSearch = "testtopic1";

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAcls(anyString(), any(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterAcls());
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(handleDbRequests.getSyncAcls(anyString(), anyInt())).thenReturn(getAclsSOT0());
    when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");

    List<AclInfo> aclList =
        aclSyncControllerService.getSyncAcls(envSelected, pageNo, "", topicNameSearch, "");

    assertThat(aclList).isEmpty();
  }

  @Test
  @Order(20)
  public void getAclsSyncTrue2() throws KlawException {
    String envSelected = "1", pageNo = "1", topicNameSearch = "test";
    boolean isSyncAcls = true;

    stubUserInfo();
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(clusterApiService.getAcls(anyString(), any(), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterAcls());
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(getAvailableTeams());
    when(handleDbRequests.getSyncAcls(anyString(), anyInt())).thenReturn(getAclsSOT0());
    when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
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
    aclReq.setTopictype(AclType.CONSUMER.value);

    aclList.add(aclReq);

    return aclList;
  }

  private List<Acl> getAclsSOT(String topicName) {
    List<Acl> aclList = new ArrayList();

    Acl aclReq = new Acl();
    aclReq.setReq_no(1001);
    aclReq.setTopicname(topicName);
    aclReq.setTeamId(1);
    aclReq.setAclip("2.1.2.1");
    aclReq.setAclssl(null);
    aclReq.setEnvironment("1");
    aclReq.setConsumergroup("mygrp1");
    aclReq.setTopictype(AclType.CONSUMER.value);

    aclList.add(aclReq);

    return aclList;
  }

  private AclRequestsModel getAclRequest() {
    AclRequestsModel aclReq = new AclRequestsModel();
    aclReq.setTopicname("testtopic");
    aclReq.setTopictype("producer");
    aclReq.setRequestingteam(1);
    aclReq.setReq_no(112);
    aclReq.setEnvironment("1");
    aclReq.setAclPatternType(AclPatternType.LITERAL.value);
    return aclReq;
  }

  private AclRequests getAclRequestDao() {
    AclRequests aclReq = new AclRequests();
    aclReq.setTopicname("testtopic");
    aclReq.setTopictype("producer");
    aclReq.setRequestingteam(1);
    aclReq.setReq_no(112);
    aclReq.setEnvironment("1");
    aclReq.setUsername("kwuserb");
    aclReq.setAclstatus("created");
    aclReq.setAcl_ip("1.2.3.4");
    return aclReq;
  }

  private List<AclRequests> getAclRequests(String topicPrefix, int size) {
    List<AclRequests> listReqs = new ArrayList<>();
    AclRequests aclReq;

    for (int i = 0; i < size; i++) {
      aclReq = new AclRequests();
      aclReq.setEnvironment("1");
      aclReq.setTopicname(topicPrefix + i);
      aclReq.setTopictype("producer");
      aclReq.setRequestingteam(1);
      aclReq.setReq_no(100 + i);
      aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
      listReqs.add(aclReq);
    }
    return listReqs;
  }
}
