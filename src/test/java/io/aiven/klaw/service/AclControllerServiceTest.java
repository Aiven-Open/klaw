package io.aiven.klaw.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.AclRequestsModel;
import io.aiven.klaw.model.SyncAclUpdates;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
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

  @Mock private HandleDbRequests handleDbRequests;

  @Mock private ManageDatabase manageDatabase;

  @Mock private CommonUtilsService commonUtilsService;

  @Mock private RolesPermissionsControllerService rolesPermissionsControllerService;

  @Mock private Map<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

  @Mock private MailUtils mailService;

  @Mock private UserInfo userInfo;

  private AclControllerService aclControllerService;
  private Env env;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.aclControllerService = new AclControllerService(clusterApiService, mailService);

    this.env = new Env();
    env.setName("DEV");
    env.setId("1");
    ReflectionTestUtils.setField(aclControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(aclControllerService, "commonUtilsService", commonUtilsService);
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
  public void createAcl() {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequest();
    copyProperties(aclRequests, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();

    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", "success");
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);
    stubUserInfo();

    String result = aclControllerService.createAcl(aclRequests);
    assertEquals("{\"result\":\"success\"}", result);
  }

  @Test
  @Order(2)
  public void updateSyncAcls() {
    stubUserInfo();
    when(handleDbRequests.addToSyncacls(anyList())).thenReturn("success");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    Map<String, String> result =
        aclControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertEquals("success", result.get("result"));
  }

  @Test
  @Order(3)
  public void updateSyncAclsFailure1() {
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    Map<String, String> result =
        aclControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertEquals("Not Authorized.", result.get("result"));
  }

  @Test
  @Order(4)
  public void updateSyncAclsFailure2() {
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.addToSyncacls(anyList())).thenThrow(new RuntimeException("Error"));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    Map<String, String> result =
        aclControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertThat(result.get("result"), CoreMatchers.containsString("Failure"));
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }

  @Test
  @Order(5)
  public void updateSyncAclsFailure3() {
    List<SyncAclUpdates> updates = new ArrayList<>();
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    Map<String, String> result = aclControllerService.updateSyncAcls(updates);
    assertEquals("No record updated.", result.get("result"));
  }

  @Test
  @Order(6)
  public void updateSyncAclsFailure4() {
    when(handleDbRequests.addToSyncacls(anyList())).thenThrow(new RuntimeException("Error"));
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    Map<String, String> result =
        aclControllerService.updateSyncAcls(utilMethods.getSyncAclsUpdates());
    assertThat(result.get("result"), CoreMatchers.containsString("Failure"));
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
    assertEquals(0, aclReqs.size());
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

    assertEquals(0, listReqs.size());
  }

  @Test
  @Order(9)
  public void deleteAclRequests() {
    String req_no = "1001";
    when(handleDbRequests.deleteAclRequest(Integer.parseInt(req_no), 1)).thenReturn("success");
    String result = aclControllerService.deleteAclRequests(req_no);
    // assertEquals("{\"result\":\"null\"}", result);
  }

  @Test
  @Order(10)
  public void deleteAclRequestsFailure() {
    String req_no = "1001";
    when(handleDbRequests.deleteAclRequest(Integer.parseInt(req_no), 1)).thenReturn("failure");
    String result = aclControllerService.deleteAclRequests(req_no);
    // assertEquals("{\"result\":\"null\"}", result);
  }

  @Test
  @Order(11)
  public void approveAclRequests() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.selectAcl(anyInt(), anyInt())).thenReturn(aclReq);
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(resultMap, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyString())).thenReturn("success");
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    String result = aclControllerService.approveAclRequests("112");
    assertEquals("{\"result\":\"success\"}", result);
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
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "failure");
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(resultMap, HttpStatus.OK));

    String result = aclControllerService.approveAclRequests(req_no);
    assertEquals("{\"result\":\"failure\"}", result);
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
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(resultMap, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyString()))
        .thenThrow(new RuntimeException("Error"));

    String result = aclControllerService.approveAclRequests(req_no);
    assertThat(result, CoreMatchers.containsString("failure"));
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

    String result = aclControllerService.approveAclRequests(req_no);
    assertEquals("{\"result\":\"This request does not exist anymore.\"}", result);
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
    when(handleDbRequests.declineAclRequest(any(), any())).thenReturn("success");

    String result = aclControllerService.declineAclRequests(req_no, "");
    assertEquals("{\"result\":\"success\"}", result);
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

    String result = aclControllerService.declineAclRequests(req_no, "Reason");
    // assertEquals("{\"result\":\"null\"}", result);
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

    List<AclInfo> aclList = aclControllerService.getAcls(topicNameSearch, "").getAclInfoList();

    assertEquals(1, aclList.size());

    assertEquals(topicNameSearch, aclList.get(0).getTopicname());
    assertEquals("mygrp1", aclList.get(0).getConsumergroup());
    assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
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

    List<AclInfo> aclList = aclControllerService.getAcls(topicNameSearch, "").getAclInfoList();

    assertEquals(1, aclList.size());

    assertEquals(topicNameSearch, aclList.get(0).getTopicname());
    assertEquals("mygrp1", aclList.get(0).getConsumergroup());
    assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
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
        aclControllerService.getSyncAcls(envSelected, pageNo, "", topicNameSearch, "");

    assertEquals(0, aclList.size());
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
        aclControllerService.getSyncAcls(envSelected, pageNo, "", topicNameSearch, "");

    assertEquals(0, aclList.size());
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
    aclReq.setTopictype("Consumer");

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
    aclReq.setTopictype("Consumer");

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
    aclReq.setAclPatternType("LITERAL");
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
