package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.AclInfo;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AclControllerServiceTest {

    private UtilMethods utilMethods;

    @Mock
    ClusterApiService clusterApiService;

    @Mock
    HandleDbRequests handleDbRequests;

    @Mock
    ManageDatabase manageDatabase;

    @Mock
    UtilService utilService;

    AclControllerService aclControllerService;
    Env env;

    @Before
    public void setUp() throws Exception {
        utilMethods = new UtilMethods();
        this.aclControllerService = new AclControllerService(clusterApiService, utilService);

        this.env = new Env();
        env.setHost("101.10.11.11");
        env.setPort("9092");
        env.setName("DEV");
        ReflectionTestUtils.setField(aclControllerService, "manageDatabase", manageDatabase);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createAcl() {
        AclRequests aclRequests = getAclRequest();

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.requestForAcl(aclRequests)).thenReturn("success");

        String result = aclControllerService.createAcl(aclRequests);
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void updateSyncAcls() {
        String topicName = "testtopic";
        String updateSyncAcls = "fdsDZD34"+ "-----" + topicName + "-----" + "Team1" + "-----"
                + "testconsumergroup" + "-----" + "10.11.11.223" + "-----"+null+"-----"+"consumer"+"\n";
        String envSelected = "DEV";

        when(utilService.checkAuthorizedSU()).thenReturn(true);
        when(handleDbRequests.addToSyncacls(anyList())).thenReturn("success");

        String result = aclControllerService.updateSyncAcls(updateSyncAcls, envSelected);
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void updateSyncAclsFailure1() {
        String topicName = "testtopic";

        String updateSyncAcls = topicName + "-----" + "Team1" + "-----"
                + "testconsumergroup" + "-----" + "10.11.11.223" + "-----"+null+"-----"+"consumer"+"\n";

        String envSelected = "DEV";

        when(utilService.checkAuthorizedSU()).thenReturn(false);

        String result = aclControllerService.updateSyncAcls(updateSyncAcls, envSelected);
        assertEquals("{\"result\":\"Not Authorized\"}",result);
    }

    @Test
    public void updateSyncAclsFailure2() {
        String topicName = "testtopic";

        String updateSyncAcls = "fdsDZD34"+ "-----" + topicName + "-----" + "Team1" + "-----"
                + "testconsumergroup" + "-----" + "10.11.11.223" + "-----"+null+"-----"+"consumer"+"\n";

        String envSelected = "DEV";

        when(utilService.checkAuthorizedSU()).thenReturn(true);
        when(handleDbRequests.addToSyncacls(anyList())).thenThrow(new RuntimeException("Error"));

        String result = aclControllerService.updateSyncAcls(updateSyncAcls, envSelected);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void updateSyncAclsFailure3() {
        String updateSyncAcls = null;
        String envSelected = "DEV";

        when(utilService.checkAuthorizedSU()).thenReturn(true);

        String result = aclControllerService.updateSyncAcls(updateSyncAcls, envSelected);
        assertEquals("{\"result\":\"No records to update\"}",result);
    }

    @Test
    public void updateSyncAclsFailure4() {
        String topicName = "testtopic";

        String updateSyncAcls = "fdsDZD34"+ "-----" + topicName + "-----" + "Team1" + "-----"
                + "testconsumergroup" + "-----" + "10.11.11.223" + "-----"+null+"-----"+"consumer"+"\n";

        String envSelected = "DEV";

        when(utilService.checkAuthorizedSU()).thenReturn(true);
        when(handleDbRequests.addToSyncacls(anyList())).thenThrow(new RuntimeException("Error"));

        String result = aclControllerService.updateSyncAcls(updateSyncAcls, envSelected);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void getAclRequests() {
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.getAllAclRequests(anyString())).thenReturn(getAclRequests("testtopic",5));
        List<AclRequests> aclReqs =  aclControllerService.getAclRequests("1");
        assertEquals(aclReqs.size(),5);
    }

    @Test
    public void getCreatedAclRequests() {
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.getCreatedAclRequests(anyString())).thenReturn(getAclRequests("testtopic",16));
        List<List<AclRequests>> listReqs = aclControllerService.getCreatedAclRequests();

        assertEquals(listReqs.size(),6);
        assertEquals(listReqs.get(1).size(),3);
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

        when(utilService.checkAuthorizedAdmin()).thenReturn(true);
        when(utilService.getUserName()).thenReturn("uiuser1");
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

        when(utilService.checkAuthorizedAdmin()).thenReturn(true);
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);
        when(clusterApiService.approveAclRequests(any())).thenReturn(new ResponseEntity<>("failure",HttpStatus.OK));

        String result = aclControllerService.approveAclRequests(req_no);
        assertEquals("{\"result\":\"failure\"}", result);
    }

    @Test
    public void approveAclRequestsFailure2() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = getAclRequest();

        when(utilService.checkAuthorizedAdmin()).thenReturn(true);
        when(utilService.getUserName()).thenReturn("uiuser1");
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

        when(utilService.checkAuthorizedAdmin()).thenReturn(true);
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);

        String result = aclControllerService.approveAclRequests(req_no);
        assertEquals("{\"result\":\"Record not found !\"}", result);
    }

    @Test
    public void declineAclRequests() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = getAclRequest();

        when(utilService.checkAuthorizedAdmin()).thenReturn(true);
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);
        when(handleDbRequests.declineAclRequest(any(), any())).thenReturn("success");

        String result = aclControllerService.declineAclRequests(req_no);
        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void declineAclRequestsFailure() throws KafkawizeException {
        String req_no = "d32fodFqD";
        AclRequests aclReq = new AclRequests();

        when(utilService.checkAuthorizedAdmin()).thenReturn(true);
        when(handleDbRequests.selectAcl(req_no)).thenReturn(aclReq);

        String result = aclControllerService.declineAclRequests(req_no);
        assertEquals("{\"result\":\"Record not found !\"}", result);
    }

    @Test
    public void getAclsSyncFalse1() throws KafkawizeException {
        String envSelected = "DEV", pageNo = "1", topicNameSearch = "testtopic1";
        boolean isSyncAcls = false;

        when(handleDbRequests.selectEnvDetails(envSelected)).thenReturn(this.env);
        when(clusterApiService.getAcls(anyString()))
                .thenReturn(utilMethods.getClusterAcls());
        when(handleDbRequests.getSyncAcls(envSelected)).thenReturn(getAclsSOT(topicNameSearch));

        List<AclInfo> aclList =  aclControllerService.getAcls(envSelected, pageNo, topicNameSearch, isSyncAcls);

        assertEquals(1, aclList.size());
        assertEquals("testtopic1",aclList.get(0).getTopicname());
        assertEquals("mygrp1",aclList.get(0).getConsumergroup());
        assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
    }

    @Test
    public void getAclsSyncFalse2() throws KafkawizeException {
        String envSelected = "DEV", pageNo = "1", topicNameSearch = "testtopic";
        boolean isSyncAcls = false;

        when(handleDbRequests.selectEnvDetails(envSelected)).thenReturn(this.env);
        when(clusterApiService.getAcls(any()))
                .thenReturn(utilMethods.getClusterAcls());
        when(handleDbRequests.getSyncAcls(envSelected)).thenReturn(getAclsSOT0());

        List<AclInfo> aclList =  aclControllerService.getAcls(envSelected, pageNo, topicNameSearch, isSyncAcls);

        assertEquals(aclList, null);
    }

    @Test
    public void getAclsSyncTrue1() throws KafkawizeException {
        String envSelected = "DEV", pageNo = "1", topicNameSearch = "testtopic1";
        boolean isSyncAcls = true;

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(utilService.checkAuthorizedSU()).thenReturn(true);
        when(handleDbRequests.selectEnvDetails(envSelected)).thenReturn(this.env);
        when(clusterApiService.getAcls(any()))
                .thenReturn(utilMethods.getClusterAcls());
        when(handleDbRequests.selectAllTeamsOfUsers(any())).thenReturn(getAvailableTeams());
        when(handleDbRequests.getSyncAcls(envSelected)).thenReturn(getAclsSOT0());

        List<AclInfo> aclList =  aclControllerService.getAcls(envSelected, pageNo, topicNameSearch, isSyncAcls);

        assertEquals(1, aclList.size());
        assertEquals("testtopic1",aclList.get(0).getTopicname());
        assertEquals("mygrp1",aclList.get(0).getConsumergroup());
        assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
    }

    @Test
    public void getAclsSyncTrue2() throws KafkawizeException {
        String envSelected = "DEV", pageNo = "1", topicNameSearch = "test";
        boolean isSyncAcls = true;

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(utilService.checkAuthorizedSU()).thenReturn(true);
        when(handleDbRequests.selectEnvDetails(envSelected)).thenReturn(this.env);
        when(clusterApiService.getAcls(any()))
                .thenReturn(utilMethods.getClusterAcls());
        when(handleDbRequests.selectAllTeamsOfUsers(any())).thenReturn(getAvailableTeams());
        when(handleDbRequests.getSyncAcls(envSelected)).thenReturn(getAclsSOT0());

        List<AclInfo> aclList =  aclControllerService.getAcls(envSelected, pageNo, topicNameSearch, isSyncAcls);

        assertEquals(1, aclList.size());
        assertEquals("testtopic1",aclList.get(0).getTopicname());
        assertEquals("mygrp1",aclList.get(0).getConsumergroup());
        assertEquals("2.1.2.1", aclList.get(0).getAcl_ip());
    }

    private List<Team> getAvailableTeams(){

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



    private List<Acl> getAclsSOT0(){
        List<Acl> aclList = new ArrayList();

        Acl aclReq = new Acl();
        aclReq.setReq_no("fsd432FD");
        aclReq.setTopicname("testtopic1");
        aclReq.setTeamname("Team1");
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
        aclReq.setTeamname("Team1");
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
        aclReq.setRequestingteam("Team1");
        aclReq.setReq_no("112");
        return aclReq;
    }

    private List<AclRequests> getAclRequests(String topicPrefix, int size){
        List<AclRequests> listReqs = new ArrayList<>();
        AclRequests aclReq ;

        for(int i=0;i<size;i++) {
            aclReq = new AclRequests();
            aclReq.setTopicname(topicPrefix+i);
            aclReq.setTopictype("producer");
            aclReq.setRequestingteam("Team1");
            aclReq.setReq_no("100"+i);
            listReqs.add(aclReq);
        }
        return listReqs;
    }
}