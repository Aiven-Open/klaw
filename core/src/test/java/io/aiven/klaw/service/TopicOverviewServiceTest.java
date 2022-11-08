package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.AclType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class TopicOverviewServiceTest {

  private UtilMethods utilMethods;
  @Mock private UserDetails userDetails;
  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private ManageDatabase manageDatabase;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private MailUtils mailService;
  @Mock private UserInfo userInfo;

  @Mock private ClusterApiService clusterApiService;

  private TopicOverviewService topicOverviewService;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.topicOverviewService = new TopicOverviewService(mailService);

    Env env = new Env();
    env.setName("DEV");
    env.setId("1");
    ReflectionTestUtils.setField(topicOverviewService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(topicOverviewService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(topicOverviewService, "clusterApiService", clusterApiService);
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

    List<AclInfo> aclList = topicOverviewService.getTopicOverview(topicNameSearch).getAclInfoList();

    assertThat(aclList).hasSize(1);

    assertThat(aclList.get(0).getTopicname()).isEqualTo(topicNameSearch);
    assertThat(aclList.get(0).getConsumergroup()).isEqualTo("mygrp1");
    assertThat(aclList.get(0).getAcl_ip()).isEqualTo("2.1.2.1");
  }

  @Test
  @Order(2)
  public void getAclsSyncFalse2() {
    String topicNameSearch = "testnewtopic1";

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

    List<AclInfo> aclList = topicOverviewService.getTopicOverview(topicNameSearch).getAclInfoList();

    assertThat(aclList).hasSize(1);

    assertThat(aclList.get(0).getTopicname()).isEqualTo(topicNameSearch);
    assertThat(aclList.get(0).getConsumergroup()).isEqualTo("mygrp1");
    assertThat(aclList.get(0).getAcl_ip()).isEqualTo("2.1.2.1");
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

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
