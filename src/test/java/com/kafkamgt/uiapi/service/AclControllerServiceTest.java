package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.AclInfo;
import com.kafkamgt.uiapi.model.SyncAclUpdates;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AclControllerServiceTest {

    private UtilMethods utilMethods;

    @Mock
    private
    UserDetails userDetails;

    @Mock
    private
    ClusterApiService clusterApiService;

    @Mock
    private
    HandleDbRequests handleDbRequests;

    @Mock
    private
    ManageDatabase manageDatabase;

    @Mock
    private
    UtilService utilService;

    private AclControllerService aclControllerService;
    private Env env;

    @Before
    public void setUp() throws Exception {
        utilMethods = new UtilMethods();
        this.aclControllerService = new AclControllerService(clusterApiService, utilService);

        this.env = new Env();
        env.setHost("101.10.11.11:9092");
        env.setName("DEV");
        env.setProtocol("PLAINTEXT");
        ReflectionTestUtils.setField(aclControllerService, "manageDatabase", manageDatabase);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
        loginMock();
    }

    @After
    public void tearDown() throws Exception {
    }

    private void loginMock(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void createAcl() {
        AclRequests aclRequests = getAclRequest();
        List<Topic> topicList = utilMethods.getTopics();

        when(handleDbRequests.getTopics(any())).thenReturn(topicList);
        when(handleDbRequests.requestForAcl(aclRequests)).thenReturn("success");

        String result = aclControllerService.createAcl(aclRequests);
        assertEquals("{\"result\":\"success\"}",result);
    }



    @Test
    public void getAclRequests() {
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getAllAclRequests(anyString())).thenReturn(getAclRequests("testtopic",5));
        List<AclRequests> aclReqs =  aclControllerService.getAclRequests("1");
        assertEquals(aclReqs.size(),5);
    }

    @Test
    public void getCreatedAclRequests() {
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getCreatedAclRequests(anyString())).thenReturn(getAclRequests("testtopic",16));
        List<List<AclRequests>> listReqs = aclControllerService.getCreatedAclRequests();

        assertEquals(16, listReqs.size());
        assertEquals(listReqs.get(1).size(),1);
        assertEquals(listReqs.get(5).size(),1);
    }

    @Test
    public void deleteAclRequests() {
        String req_no = "d32fodFqD";
        when(handleDbRequests.deleteAclRequest(req_no)).thenReturn("success");
        String result = aclControllerService.deleteAclRequests(req_no);
        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void deleteAclRequestsFailure() {
        String req_no = "d32fodFqD";
        when(handleDbRequests.deleteAclRequest(req_no)).thenReturn("failure");
        String result = aclControllerService.deleteAclRequests(req_no);
        assertEquals("{\"result\":\"failure\"}", result);
    }

    @Test
    public void approveAclRequests() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = getAclRequest();

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);
        when(clusterApiService.approveAclRequests(any())).thenReturn(new ResponseEntity<>("success",HttpStatus.OK));
        when(handleDbRequests.updateAclRequest(any(), any())).thenReturn("success");

        String result = aclControllerService.approveAclRequests(req_no);
        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void approveAclRequestsFailure1() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = getAclRequest();

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);
        when(clusterApiService.approveAclRequests(any())).thenReturn(new ResponseEntity<>("failure",HttpStatus.OK));

        String result = aclControllerService.approveAclRequests(req_no);
        assertEquals("{\"result\":\"failure\"}", result);
    }

    @Test
    public void approveAclRequestsFailure2() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = getAclRequest();

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);
        when(clusterApiService.approveAclRequests(any())).thenReturn(new ResponseEntity<>("success",HttpStatus.OK));
        when(handleDbRequests.updateAclRequest(any(), any())).thenThrow(new RuntimeException("Error"));

        String result = aclControllerService.approveAclRequests(req_no);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void approveAclRequestsFailure3() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = new AclRequests();

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);

        String result = aclControllerService.approveAclRequests(req_no);
        assertEquals("{\"result\":\"Record not found !\"}", result);
    }

    @Test
    public void declineAclRequests() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = getAclRequest();

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);
        when(handleDbRequests.declineAclRequest(any(), any())).thenReturn("success");

        String result = aclControllerService.declineAclRequests(req_no);
        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void declineAclRequestsFailure() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = new AclRequests();

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);

        String result = aclControllerService.declineAclRequests(req_no);
        assertEquals("{\"result\":\"Record not found !\"}", result);
    }

    @Test
    public void getAclsSyncFalse1() throws KafkawizeException {
        String env="DEV",topicNameSearch = "testtopic";


        when(handleDbRequests.selectAllTeamsOfUsers(userDetails.getUsername())).thenReturn(utilMethods.getTeams());
        when(handleDbRequests.getTopics(topicNameSearch)).thenReturn(utilMethods.getTopics(topicNameSearch));
        when(handleDbRequests.getSyncAcls(env, topicNameSearch)).thenReturn(getAclsSOT(topicNameSearch));

        List<AclInfo> aclList =  aclControllerService.getAcls(topicNameSearch).getAclInfoList();

        assertEquals(1, aclList.size());

        assertEquals(topicNameSearch, aclList.get(0).getTopicname());
        assertEquals("mygrp1",aclList.get(0).getConsumergroup());
        assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
    }

    @Test
    public void getAclsSyncFalse2() throws KafkawizeException {
        String env="DEV",topicNameSearch = "testnewtopic1";

        when(handleDbRequests.selectAllTeamsOfUsers(userDetails.getUsername())).thenReturn(utilMethods.getTeams());
        when(handleDbRequests.getTopics(topicNameSearch)).thenReturn(utilMethods.getTopics());
        when(handleDbRequests.getSyncAcls(env, topicNameSearch)).thenReturn(getAclsSOT(topicNameSearch));

        List<AclInfo> aclList =  aclControllerService.getAcls(topicNameSearch).getAclInfoList();

        assertEquals(1, aclList.size());

        assertEquals(topicNameSearch,aclList.get(0).getTopicname());
        assertEquals("mygrp1",aclList.get(0).getConsumergroup());
        assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
    }



    private List<Team> getAvailableTeams(){

        Team Octopus = new Team();
        Octopus.setTeamname("Octopus");

        Team team2 = new Team();
        team2.setTeamname("Team2");

        Team team3 = new Team();
        team3.setTeamname("Team3");

        List<Team> teamList = new ArrayList<>();
        teamList.add(Octopus);
        teamList.add(team2);
        teamList.add(team3);

        return teamList;
    }



    private List<Acl> getAclsSOT0(){
        List<Acl> aclList = new ArrayList();

        Acl aclReq = new Acl();
        aclReq.setReq_no("fsd432FD");
        aclReq.setTopicname("testtopic1");
        aclReq.setTeamname("Octopus");
        aclReq.setAclip("2.1.2.1");
        aclReq.setAclssl(null);
        aclReq.setConsumergroup("mygrp1");
        aclReq.setTopictype("Consumer");

        aclList.add(aclReq);

        return aclList;
    }

    private List<Acl> getAclsSOT(String topicName){
        List<Acl> aclList = new ArrayList();

        Acl aclReq = new Acl();
        aclReq.setReq_no("fsd432FD");
        aclReq.setTopicname(topicName);
        aclReq.setTeamname("Octopus");
        aclReq.setAclip("2.1.2.1");
        aclReq.setAclssl(null);
        aclReq.setConsumergroup("mygrp1");
        aclReq.setTopictype("Consumer");

        aclList.add(aclReq);

        return aclList;
    }

    private AclRequests getAclRequest(){
        AclRequests aclReq = new AclRequests();
        aclReq.setTopicname("testtopic");
        aclReq.setTopictype("producer");
        aclReq.setRequestingteam("Octopus");
        aclReq.setReq_no("112");
        aclReq.setEnvironment("DEV");
        return aclReq;
    }

    private List<AclRequests> getAclRequests(String topicPrefix, int size){
        List<AclRequests> listReqs = new ArrayList<>();
        AclRequests aclReq ;

        for(int i=0;i<size;i++) {
            aclReq = new AclRequests();
            aclReq.setTopicname(topicPrefix+i);
            aclReq.setTopictype("producer");
            aclReq.setRequestingteam("Octopus");
            aclReq.setReq_no("100"+i);
            aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
            listReqs.add(aclReq);
        }
        return listReqs;
    }
}