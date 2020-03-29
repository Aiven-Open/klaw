package com.kafkamgt.integrationtests.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UiapiApplication;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.controller.TopicController;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.model.TopicInfo;
import com.kafkamgt.uiapi.service.ClusterApiService;
import com.kafkamgt.uiapi.service.TopicControllerService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes=UiapiApplication.class)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations="classpath:test-application-rdbms.properties")
@DirtiesContext
@EnableAsync
public class TopicControllerIT {

    private UtilMethods utilMethods;

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    ClusterApiService clusterApiService;

    @Before
    public void setup() throws Exception {
        utilMethods = new UtilMethods();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // Create topic requests
    @Test
    public void test01() throws Exception {
        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user("uiuser1").password("user"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Query topic requests in created state
    @Test
    public void test02() throws Exception {
        List<List<TopicRequest>> topicReqs = utilMethods.getTopicRequestsList();

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getCreatedTopicRequests").with(user("uiuser1").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // Query topic requests in created and approved state
    @Test
    public void test03() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicRequests").with(user("uiuser1").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // approve topic - creates topic in cluster
    @Test
    public void test04() throws Exception {
        when(clusterApiService.approveTopicRequests(anyString(), any()))
                .thenReturn(new ResponseEntity<>("success",HttpStatus.OK));

        login("uiuser4","user", "ADMIN");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequests").with(user("uiuser4").password("user"))
                .param("topicName","testtopic")
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // decline topic - topic in cluster
    @Test
    public void test05() throws Exception {

        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic1");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user("uiuser1").password("user"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        login("uiuser4","user", "ADMIN");

        response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequestsDecline").with(user("uiuser4").password("user"))
                .param("topicName","testtopic1")
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // get team of topic
    @Test
    public void test06() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicTeam").with(user("uiuser1").password("user"))
                .param("topicName","testtopic")
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Topic response = new ObjectMapper().readValue(res, Topic.class);
        assertEquals("Team1", response.getTeamname());
    }

    // Update team of a topic
    @Test
    public void test07() throws Exception {

        String env = "DEV";
        String teamSelected = "Team2";
        String syncTopicsStr = "testtopic" + "-----" + teamSelected;

        login("superuser","user", "SUPERUSER");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/updateSyncTopics").with(user("superuser").password("user"))
                .param("updatedSyncTopics",syncTopicsStr)
                .param("envSelected",env)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // delete a topic request of his own
    @Test
    public void test08() throws Exception {

        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic2");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user("uiuser1").password("user"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/deleteTopicRequests")
                .param("topicName","testtopic2,DEV").with(user("uiuser1").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // get topics from cluster
    @Test
    public void test09() throws Exception {
        when(clusterApiService.getAllTopics(anyString())).thenReturn(utilMethods.getClusterApiTopics("testtopic",10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopics").with(user("uiuser1").password("user"))
                .param("env","DEV")
                .param("pageNo","1")
                .param("topicnamesearch","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<TopicInfo>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(4, response.size());
        assertEquals(3, response.get(0).size());
    }

    //get only topic names
    @Test
    public void test10() throws Exception {
        when(clusterApiService.getAllTopics(anyString())).thenReturn(utilMethods.getClusterApiTopics("testtopic",10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicsOnly").with(user("uiuser1").password("user"))
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(10, response.size());
    }

    // get all topics which can be updated with new team ids
    @Test
    public void test11() throws Exception {
        when(clusterApiService.getAllTopics(anyString())).thenReturn(utilMethods.getClusterApiTopics("testtopic",10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getSyncTopics").with(user("uiuser1").password("user"))
                .param("env","DEV")
                .param("pageNo","1")
                .param("topicnamesearch","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(10, response.size());
    }

    private void login(String user, String pwd, String role) throws Exception {
        mvc.perform(get("/login").with(user(user).password(pwd).roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/getExecAuth").with(user(user).password(pwd).roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

    }
}