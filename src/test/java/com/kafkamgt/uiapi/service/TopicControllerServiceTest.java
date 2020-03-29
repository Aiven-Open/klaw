package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.TopicInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerServiceTest {

    @Mock
    ClusterApiService clusterApiService;

    @Mock
    UserDetails userDetails;

    @Mock
    ManageDatabase manageDatabase;

    @Mock
    HandleDbRequests handleDbRequests;

    @Mock
    UtilService utilService;

    TopicControllerService topicControllerService;

    Env env;

    UtilMethods utilMethods;

    @Before
    public void setUp() throws Exception {
        this.topicControllerService = new TopicControllerService(clusterApiService, utilService);
        utilMethods = new UtilMethods();
        this.env = new Env();
        env.setHost("101.10.11.11");
        env.setPort("9092");
        env.setName("DEV");
        ReflectionTestUtils.setField(topicControllerService, "manageDatabase", manageDatabase);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createTopicsSuccess() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);
        when(handleDbRequests.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getCorrectTopic());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void createTopicsSuccess1() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);
        when(handleDbRequests.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void createTopicsSuccess2() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);
        when(handleDbRequests.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getFailureTopic1());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure1() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=abc,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure2() throws KafkawizeException {
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure3() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=abc,max.partitions=4");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test
    public void updateSyncTopicsSuccess() {
        String env = "DEV";
        String teamSelected = "Team1";
        String syncTopicsStr = "demotopic101" + "-----" + teamSelected;

        when(utilService.checkAuthorizedSU()).thenReturn(true);
        when(handleDbRequests.addToSynctopics(any())).thenReturn("success");

        String result = topicControllerService.updateSyncTopics(syncTopicsStr, env);

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void updateSyncTopicsNoUpdate() {
        String env = "DEV";
        String syncTopicsStr = "";

        when(utilService.checkAuthorizedSU()).thenReturn(true);

        String result = topicControllerService.updateSyncTopics(syncTopicsStr, env);

        assertEquals("{\"result\":\"No record updated.\"}",result);
    }

    @Test
    public void updateSyncTopicsNotAuthorized() {
        String env = "DEV";
        String teamSelected = "Team1";
        String syncTopicsStr = "demotopic101" + "-----" + teamSelected;

        when(utilService.checkAuthorizedSU()).thenReturn(false);

        String result = topicControllerService.updateSyncTopics(syncTopicsStr, env);

        assertEquals("{\"result\":\"Not Authorized\"}",result);
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

        when(utilService.checkAuthorizedAdmin_SU()).thenReturn(true);
        when(utilService.getUserName()).thenReturn("uiuser1");
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

        when(utilService.checkAuthorizedAdmin_SU()).thenReturn(true);
        when(handleDbRequests.selectTopicRequestsForTopic(topicName, env)).thenReturn(topicRequest);
        when(clusterApiService.approveTopicRequests(topicName, topicRequest))
                .thenReturn(new ResponseEntity<String>("failure error",HttpStatus.OK));

        String result = topicControllerService.approveTopicRequests(topicName, env);

        assertEquals("{\"result\":\"failure error\"}",result);
    }

    @Test
    public void getAllTopics() throws Exception {
        String envSel = "DEV";

        when(handleDbRequests.selectEnvDetails(envSel)).thenReturn(this.env);
        when(clusterApiService.getAllTopics(any()))
                .thenReturn(utilMethods.getClusterApiTopics("topic",10));

        List<String> result = topicControllerService.getAllTopics(envSel);
        assertEquals(result.size(),10);
        assertEquals(result.get(0),"topic0");
        assertEquals(result.get(9),"topic9");
    }

    @Test
    public void getTopicsSuccess1() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "top";

        when(handleDbRequests.selectEnvDetails(envSel)).thenReturn(this.env);
        when(clusterApiService.getAllTopics(this.env.getHost()+":"+this.env.getPort()))
                .thenReturn(utilMethods.getClusterApiTopics("topic",10));
        when(handleDbRequests.getSyncTopics(envSel)).thenReturn(getSyncTopics("topic",4));

        List<List<TopicInfo>> topicsList = topicControllerService.getTopics(envSel, pageNo, topicNameSearch);

        assertEquals(topicsList.size(),4);
    }

    @Test
    public void getTopicsSuccess2() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "top";

        when(handleDbRequests.selectEnvDetails(envSel)).thenReturn(this.env);
        when(clusterApiService.getAllTopics(this.env.getHost()+":"+this.env.getPort()))
                .thenReturn(utilMethods.getClusterApiTopics("topic",30));
        when(handleDbRequests.getSyncTopics(envSel)).thenReturn(getSyncTopics("topic",12));

        List<List<TopicInfo>> topicsList = topicControllerService.getTopics(envSel, pageNo, topicNameSearch);

        assertEquals(topicsList.size(),7);
        assertEquals(topicsList.get(0).get(0).getTeamname(),"Team1");
        assertEquals(topicsList.get(0).get(1).getTeamname(),"Team1");
        assertEquals(topicsList.get(0).get(2).getTeamname(),"Team2");
        assertEquals(topicsList.get(0).get(2).getTotalNoPages(),"2");
    }

    // topicSearch does not exist in topic names
    @Test
    public void getTopicsSearchFailure() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "demo";

        when(handleDbRequests.selectEnvDetails(envSel)).thenReturn(this.env);
        when(clusterApiService.getAllTopics(this.env.getHost()+":"+this.env.getPort()))
                .thenReturn(utilMethods.getClusterApiTopics("topic",10));
        when(handleDbRequests.getSyncTopics(envSel)).thenReturn(getSyncTopics("topic",4));

        List<List<TopicInfo>> topicsList = topicControllerService.getTopics(envSel, pageNo, topicNameSearch);

        assertEquals(topicsList,null);
    }

    @Test
    public void getSyncTopics() throws Exception {
        String envSel = "DEV", pageNo = "1", topicNameSearch = "top";

        when(utilService.getUserDetails()).thenReturn(userDetails);
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

        when(utilService.checkAuthorizedAdmin_SU()).thenReturn(true);
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectTopicRequestsForTopic(topicName, envSel)).thenReturn(topicRequest);
        when(handleDbRequests.declineTopicRequest(topicRequest,"uiuser1")).thenReturn("success");
        String result = topicControllerService.declineTopicRequests(topicName, envSel);

        assertEquals("{\"result\":\""+ "Request declined. " + "success" + "\"}", result);
    }

    @Test
    public void getTopicRequests(){

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.getAllTopicRequests(anyString())).thenReturn(getListTopicRequests());
        List<TopicRequest> listTopicRqs = topicControllerService.getTopicRequests("1");
        assertEquals(listTopicRqs.size(), 2);
    }

    @Test
    public void getTopicTeam(){
        String topicName = "testtopic", envSel = "DEV";
        when(handleDbRequests.getTopicTeam(topicName,envSel)).thenReturn(getTopic(topicName));

        Topic topicTeam = topicControllerService.getTopicTeam(topicName, envSel);
        assertEquals(topicTeam.getTeamname(), "Team1");
    }

    private TopicRequest getCorrectTopic(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("2");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private TopicRequest getFailureTopic(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("abc");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private TopicRequest getFailureTopic1(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("-1");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private TopicRequest getTopicRequest(String name){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname(name);
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("2");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        return topicRequest;
    }

    private List<TopicRequest> getListTopicRequests(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname("testtopic1");
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("2");
        topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));

        List<TopicRequest> listReqs = new ArrayList<>();
        listReqs.add(topicRequest);

        TopicRequest topicRequest1 = new TopicRequest();
        topicRequest1.setTopicname("testtopic12");
        topicRequest1.setEnvironment(env.getHost());
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