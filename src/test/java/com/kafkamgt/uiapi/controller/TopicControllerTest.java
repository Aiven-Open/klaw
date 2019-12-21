package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.service.TopicControllerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerTest {

    @Mock
    private TopicControllerService topicControllerService;

    private TopicController topicController;

    @Before
    public void setUp() throws Exception {
        topicController = new TopicController(topicControllerService);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createTopics() throws KafkawizeException {
        TopicRequest topicRequest = new TopicRequest();
        ResponseEntity<String> response = topicController.createTopics(topicRequest);
        assertEquals(HttpStatus.OK.value(),response.getStatusCodeValue());
    }

    @Test
    public void updateSyncTopics() {
    }

    @Test
    public void getTopicStreams() {
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
    public void getTopics() {
    }

    @Test
    public void getTopicsOnly() {
    }

    @Test
    public void getSyncTopics() {
    }
}