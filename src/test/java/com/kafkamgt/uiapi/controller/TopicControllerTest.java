package com.kafkamgt.uiapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.model.SyncTopicUpdates;
import com.kafkamgt.uiapi.model.TopicInfo;
import com.kafkamgt.uiapi.service.TopicControllerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class TopicControllerTest {

    @MockBean
    private TopicControllerService topicControllerService;

    private TopicController topicController;

    private UtilMethods utilMethods;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        topicController = new TopicController();
        mvc = MockMvcBuilders
                .standaloneSetup(topicController)
                .dispatchOptions(true)
                .build();
        utilMethods = new UtilMethods();
        ReflectionTestUtils.setField(topicController, "topicControllerService", topicControllerService);
    }

    @Test
    public void createTopics() throws Exception {
        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        when(topicControllerService.createTopicsRequest(any())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }



    @Test
    public void getTopicRequests() throws Exception {
        List<TopicRequest> topicRequests = utilMethods.getTopicRequests();

        when(topicControllerService.getTopicRequests("1")).thenReturn(topicRequests);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getTopicTeam() throws Exception {
        String topicName = "testtopic";
        when(topicControllerService.getTopicTeamOnly(topicName)).thenReturn("Team1");

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicTeam")
                .param("topicName",topicName)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("Team1", res);
    }

    @Test
    public void getCreatedTopicRequests() throws Exception {
        List<List<TopicRequest>> topicReqs = utilMethods.getTopicRequestsList();
        when(topicControllerService.getCreatedTopicRequests()).thenReturn(topicReqs);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getCreatedTopicRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void deleteTopicRequests() throws Exception {
        when(topicControllerService.deleteTopicRequests(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/deleteTopicRequests")
                .param("topicName","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void approveTopicRequests() throws Exception {
        when(topicControllerService.approveTopicRequests(anyString(), anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequests")
                .param("topicName","testtopic")
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void declineTopicRequests() throws Exception {
        when(topicControllerService.declineTopicRequests(anyString(), anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequestsDecline")
                .param("topicName","testtopic")
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void getTopics() throws Exception {
        List<List<TopicInfo>> topicList = utilMethods.getTopicInfoList();

        when(topicControllerService.getTopics(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(topicList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopics")
                .param("env","DEV")
                .param("pageNo","1")
                .param("topicnamesearch","testtopic")
                .param("teamName","Team1")
                .param("topicType","")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<TopicInfo>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getTopicsOnly() throws Exception {
        List<String> topicList = Arrays.asList("testtopic1", "testtopic2");
        when(topicControllerService.getAllTopics()).thenReturn(topicList);
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicsOnly")
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(2, response.size());
    }


}