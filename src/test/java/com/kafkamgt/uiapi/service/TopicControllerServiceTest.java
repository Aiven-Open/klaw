package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerServiceTest {

    @Mock
    ClusterApiService clusterApiService;

    @Mock
    ManageTopics manageTopics;

    @Mock
    UtilService utilService;

    TopicControllerService topicControllerService;

    Env env;

    @Before
    public void setUp() throws Exception {
        this.topicControllerService = new TopicControllerService(clusterApiService, manageTopics, utilService);

        this.env = new Env();
        env.setHost("101.10.11.11");
        env.setName("DEV");

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createTopicsSuccess() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(manageTopics.selectEnvDetails(anyString())).thenReturn(env);
        when(manageTopics.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getCorrectTopic());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void createTopicsSuccess1() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(manageTopics.selectEnvDetails(anyString())).thenReturn(env);
        when(manageTopics.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void createTopicsSuccess2() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=2,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(manageTopics.selectEnvDetails(anyString())).thenReturn(env);
        when(manageTopics.requestForTopic(any())).thenReturn("success");

        String result = topicControllerService.createTopics(getFailureTopic1());

        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure1() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=abc,max.partitions=4,replication.factor=1");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(manageTopics.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure2() throws KafkawizeException {
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(manageTopics.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test(expected = KafkawizeException.class)
    public void createTopicsFailure3() throws KafkawizeException {
        this.env.setOtherParams("default.paritions=abc,max.partitions=4");
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(manageTopics.selectEnvDetails(anyString())).thenReturn(env);

        String result = topicControllerService.createTopics(getFailureTopic());

        assertEquals("{\"result\":\"failure\"}",result);
    }

    @Test
    public void updateSyncTopics() {
    }

    @Test
    public void getTopicStreams() {
    }

    @Test
    public void getPCStreamsPaginated() {
    }

    @Test
    public void getTopicRequests() {
    }

    @Test
    public void getTopicTeam() {
    }

    @Test
    public void getCreatedTopicRequests() {
    }

    @Test
    public void deleteTopicRequests() {
    }

    @Test
    public void approveTopicRequests() {
    }

    @Test
    public void getAllTopics() {
    }

    @Test
    public void getTopics() {
    }

    @Test
    public void getSyncTopics() {
    }

    @Test
    public void getTopicList() {
    }

    @Test
    public void getSyncTopicList() {
    }

    @Test
    public void createTopics1() {
    }

    @Test
    public void updateSyncTopics1() {
    }

    @Test
    public void getTopicStreams1() {
    }

    @Test
    public void getPCStreamsPaginated1() {
    }

    @Test
    public void getTopicRequests1() {
    }

    @Test
    public void getTopicTeam1() {
    }

    @Test
    public void getCreatedTopicRequests1() {
    }

    @Test
    public void deleteTopicRequests1() {
    }

    @Test
    public void approveTopicRequests1() {
    }

    @Test
    public void declineTopicRequests() {
    }

    @Test
    public void getAllTopics1() {
    }

    @Test
    public void getTopics1() {
    }

    @Test
    public void getSyncTopics1() {
    }

    @Test
    public void counterIncrement() {
    }

    @Test
    public void getTopicList1() {
    }

    @Test
    public void getSyncTopicList1() {
    }

    private TopicRequest getCorrectTopic(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("2");
        return topicRequest;
    }

    private TopicRequest getFailureTopic(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("abc");
        return topicRequest;
    }

    private TopicRequest getFailureTopic1(){

        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setEnvironment(env.getHost());
        topicRequest.setTopicpartitions("-1");
        return topicRequest;
    }
}