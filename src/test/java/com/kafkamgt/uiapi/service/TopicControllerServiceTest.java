package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.SyncTopicUpdates;
import com.kafkamgt.uiapi.model.TopicInfo;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerServiceTest {

    @Mock
    private
    ClusterApiService clusterApiService;

    @Mock
    private
    UserDetails userDetails;

    @Mock
    private
    ManageDatabase manageDatabase;

    @Mock
    private
    HandleDbRequests handleDbRequests;

    @Mock
    private
    UtilService utilService;

    private TopicControllerService topicControllerService;

    private Env env;

    private UtilMethods utilMethods;

    @Before
    public void setUp() throws Exception {
        this.topicControllerService = new TopicControllerService(clusterApiService, utilService);
        utilMethods = new UtilMethods();
        this.env = new Env();
        env.setHost("101.10.11.11");
        env.setPort("9092");
        env.setName("DEV");
        ReflectionTestUtils.setField(topicControllerService, "manageDatabase", manageDatabase);
        ReflectionTestUtils.setField(topicControllerService, "syncCluster", "DEV");
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
    public void createTopicsSuccess() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);
        when(handleDbRequests.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getCorrectTopic());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void createTopicsSuccess1() throws KafkawizeException {
        String topicName = "testtopic";
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);
        when(handleDbRequests.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void createTopicsSuccess2() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);
        when(handleDbRequests.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getFailureTopic1());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure1() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=abc,max.partitions=4,replication.factor=1");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure2() throws KafkawizeException {
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure3() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=abc,max.partitions=4");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test
    public void updateSyncTopicsSuccess() {
        String teamSelected = "Team1";
        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("result","success");

        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addToSynctopics(any())).thenReturn("success");

        HashMap<String, String> result = topicControllerService.updateSyncTopics(utilMethods.getSyncTopicUpdates());

        assertEquals("success",result.get("result"));
    }

    @Test
    public void updateSyncTopicsNoUpdate() {
        List<SyncTopicUpdates> topicUpdates = new ArrayList<>();
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);

        HashMap<String, String> result = topicControllerService.updateSyncTopics(topicUpdates);

        assertEquals("No record updated.",result.get("result"));
    }

    @Test
    public void updateSyncTopicsNotAuthorized() {
        String teamSelected = "Team1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);

        HashMap<String, String> result = topicControllerService.updateSyncTopics(utilMethods.getSyncTopicUpdates());

        assertEquals("Not Authorized.",result.get("result"));
    }

    @Test
    public void getCreatedTopicRequests1() {
        List<TopicRequest> listTopicReqs = new ArrayList<>();
        listTopicReqs.add(getCorrectTopic());
        listTopicReqs.add(getFailureTopic());

        when(handleDbRequests.getCreatedTopicRequests(any())).thenReturn(listTopicReqs);

        List<List<TopicRequest>> topicList = topicControllerService.getCreatedTopicRequests();

        assertEquals(topicList.size(),1);
        assertEquals(topicList.get(0).size(),2);
    }

    @Test
    public void getCreatedTopicRequests2() {
        List<TopicRequest> listTopicReqs = new ArrayList<>();
        listTopicReqs.add(getTopicRequest("topic1"));
        listTopicReqs.add(getTopicRequest("topic2"));
        listTopicReqs.add(getTopicRequest("topic3"));
        listTopicReqs.add(getTopicRequest("topic4"));
        listTopicReqs.add(getTopicRequest("topic5"));

        when(handleDbRequests.getCreatedTopicRequests(any())).thenReturn(listTopicReqs);

        List<List<TopicRequest>> topicList = topicControllerService.getCreatedTopicRequests();

        assertEquals(topicList.size(),3);
        assertEquals(topicList.get(0).size(),2);
        assertEquals(topicList.get(1).size(),2);
        assertEquals(topicList.get(0).get(1).getTopicname(),"topic2");
        assertEquals(topicList.get(1).get(1).getTopicname(),"topic4");
    }

    @Test
    public void deleteTopicRequests() {
        when(handleDbRequests.deleteTopicRequest("topic1","DEV")).thenReturn("success");
        String result = topicControllerService.deleteTopicRequests("topic1,DEV");
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void approveTopicRequestsSuccess() throws KafkawizeException {
        String topicName = "topic1";
        TopicRequest topicRequest = getTopicRequest(topicName);

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectTopicRequestsForTopic(topicName, "DEV")).thenReturn(topicRequest);
        when(handleDbRequests.updateTopicRequest(topicRequest, "uiuser1")).thenReturn("success");
        when(clusterApiService.approveTopicRequests(topicName, topicRequest)).thenReturn(new ResponseEntity<String>("success",HttpStatus.OK));

        String result = topicControllerService.approveTopicRequests(topicName, "DEV");

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void approveTopicRequestsFailure1() throws KafkawizeException {
        String topicName = "topic1", env = "DEV";
        TopicRequest topicRequest = getTopicRequest(topicName);

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(handleDbRequests.selectTopicRequestsForTopic(topicName, env)).thenReturn(topicRequest);
        when(clusterApiService.approveTopicRequests(topicName, topicRequest))
                .thenReturn(new ResponseEntity<String>("failure error",HttpStatus.OK));

        String result = topicControllerService.approveTopicRequests(topicName, env);

        assertEquals("{\"result\":\"failure error\"}",result);
    }

    @Test
    public void getAllTopics() throws Exception {
        String envSel = "DEV";

        when(handleDbRequests.getSyncTopics(any(),any())).thenReturn(utilMethods.getTopics());

        List<String> result = topicControllerService.getAllTopics();
        assertEquals(result.size(),1);
        assertEquals(result.get(0),"testtopic");
    }

    @Test
    public void getTopicsSuccess1() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "top";

        when(handleDbRequests.getSyncTopics(envSel, null)).thenReturn(getSyncTopics("topic",4));

        List<List<TopicInfo>> topicsList = topicControllerService.getTopics(envSel, pageNo, topicNameSearch, null);

        assertEquals(2, topicsList.size());
    }

    @Test
    public void getTopicsSuccess2() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "top";

        when(handleDbRequests.getSyncTopics(envSel, null)).thenReturn(getSyncTopics("topic",12));

        List<List<TopicInfo>> topicsList = topicControllerService.getTopics(envSel, pageNo, topicNameSearch, null);

        assertEquals(4,topicsList.size());
        assertEquals(topicsList.get(0).get(0).getTeamname(),"Team1");
        assertEquals("Team2", topicsList.get(0).get(1).getTeamname());
        assertEquals("Team1", topicsList.get(0).get(2).getTeamname());
        assertEquals("1", topicsList.get(0).get(2).getTotalNoPages());
    }

    // topicSearch does not exist in topic names
    @Test
    public void getTopicsSearchFailure() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "demo";

        when(handleDbRequests.getSyncTopics(envSel, null)).thenReturn(getSyncTopics("topic",4));

        List<List<TopicInfo>> topicsList = topicControllerService.getTopics(envSel, pageNo, topicNameSearch, null);

        assertNull(topicsList);
    }

    @Test
    public void getSyncTopics() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "top";

        when(handleDbRequests.selectEnvDetails(envSel)).thenReturn(this.env);
        when(clusterApiService.getAllTopics(this.env.getHost()+":"+this.env.getPort()))
                .thenReturn(utilMethods.getClusterApiTopics("topic",10));
        when(handleDbRequests.selectAllTeamsOfUsers(any())).thenReturn(getAvailableTeams());

        List<TopicRequest> topicRequests = topicControllerService.getSyncTopics(envSel, pageNo, topicNameSearch);
        assertEquals(topicRequests.size(),10);
    }

    @Test
    public void declineTopicRequests() throws KafkawizeException {
        String topicName = "testtopic", envSel = "DEV";
        TopicRequest topicRequest = getTopicRequest(topicName);

        when(utilService.checkAuthorizedAdmin_SU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectTopicRequestsForTopic(topicName, envSel)).thenReturn(topicRequest);
        when(handleDbRequests.declineTopicRequest(topicRequest,"uiuser1")).thenReturn("success");
        String result = topicControllerService.declineTopicRequests(topicName, envSel);

        assertEquals("{\"result\":\""+ "Request declined. " + "success" + "\"}", result);
    }

    @Test
    public void getTopicRequests(){

        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getAllTopicRequests(anyString())).thenReturn(getListTopicRequests());
        List<TopicRequest> listTopicRqs = topicControllerService.getTopicRequests("1");
        assertEquals(listTopicRqs.size(), 2);
    }

    @Test
    public void getTopicTeam(){
        String topicName = "testtopic", envSel = "DEV";
        when(handleDbRequests.getTopicTeam(topicName)).thenReturn(Arrays.asList(getTopic(topicName)));

        List<Topic> topicTeam = topicControllerService.getTopicTeam(topicName);
        assertEquals(topicTeam.get(0).getTeamname(), "Team1");
    }

    private TopicRequest getCorrectTopic(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getName());
        topicRequest.setTopicpartitions("2");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private TopicRequest getFailureTopic(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getName());
        topicRequest.setTopicpartitions("abc");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private TopicRequest getFailureTopic1(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getName());
        topicRequest.setTopicpartitions("-1");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private TopicRequest getTopicRequest(String name){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname(name);
        topicRequest.setEnvironment(env.getName());
        topicRequest.setTopicpartitions("2");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private List<TopicRequest> getListTopicRequests(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname("testtopic1");
        topicRequest.setEnvironment(env.getName());
        topicRequest.setTopicpartitions("2");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));

        List<TopicRequest> listReqs = new ArrayList<>();
        listReqs.add(topicRequest);

        TopicRequest topicRequest1 = new TopicRequest();
        topicRequest1.setTopicname("testtopic12");
        topicRequest1.setEnvironment(env.getName());
        topicRequest1.setTopicpartitions("2");
        topicRequest1.setRequesttime(new Timestamp(System.currentTimeMillis()));

        listReqs.add(topicRequest1);

        return listReqs;
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

    private Topic getTopic(String topicName){
        Topic t = new Topic();
        TopicPK topicPK = new TopicPK();
        t.setTeamname("Team1");
        t.setTopicname(topicName);
        topicPK.setTopicname(topicName);
        t.setTopicPK(topicPK);

        return t;
    }

    private List<Topic> getSyncTopics(String topicPrefix, int size){
        List<Topic> listTopics = new ArrayList<>();
        Topic t;
        TopicPK topicPK ;

        for(int i=0;i<size;i++) {
            t = new Topic();
            topicPK = new TopicPK();

            if(i%2 == 0)
                t.setTeamname("Team1");
            else
                t.setTeamname("Team2");

            t.setTopicname(topicPrefix +i);
            topicPK.setTopicname(topicPrefix +i);
            t.setTopicPK(topicPK);

            listTopics.add(t);
        }
        return listTopics;
    }
}