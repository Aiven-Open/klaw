package com.kafkamgt.integrationtests.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UiapiApplication;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.model.*;
import com.kafkamgt.uiapi.service.ClusterApiService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Add license key to application properties in test resources folder to run IT tests.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes=UiapiApplication.class)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations="classpath:test-application-rdbms.properties")
@DirtiesContext
@EnableAsync
public class TopicAclControllerIT {

    private UtilMethods utilMethods;

    private MockMvc mvc;

    private String reqId = null;

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
    public void test001() throws Exception {
        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        login("gary","user", "USER");
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user("gary").password("user").roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Query topic requests in created state
    @Test
    public void test002() throws Exception {
        List<List<TopicRequest>> topicReqs = utilMethods.getTopicRequestsList();

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getCreatedTopicRequests").with(user("gary").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // Query topic requests in created and approved state
    @Test
    public void test003() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicRequests").with(user("gary").password("user"))
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
    public void test004() throws Exception {
        when(clusterApiService.approveTopicRequests(anyString(), any()))
                .thenReturn(new ResponseEntity<>("success",HttpStatus.OK));

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequests").with(user("cris").password("user").roles("ADMIN"))
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
    public void test005() throws Exception {

        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic1");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        login("gary","user", "USER");
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user("gary").password("user").roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        login("cris","user", "ADMIN");

        response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequestsDecline").with(user("cris").password("user").roles("ADMIN"))
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
    public void test006() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicTeam").with(user("gary").password("user"))
                .param("topicName","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(res, CoreMatchers.containsString("Octopus"));
    }

    // delete a topic request of his own
    @Test
    public void test008() throws Exception {

        TopicRequest addTopicRequest = utilMethods.getTopicRequest("testtopic2");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        login("gary","user", "USER");
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user("gary").password("user").roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/deleteTopicRequests")
                .param("topicName","testtopic2,DEV").with(user("gary").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // get topics from cluster
    @Test
    public void test009() throws Exception {
        when(clusterApiService.getAllTopics(anyString(), eq("PLAINTEXT")))
                .thenReturn(utilMethods.getClusterApiTopics("testtopic",10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopics").with(user("gary").password("user"))
                .param("env","DEV")
                .param("pageNo","1")
                .param("topicnamesearch","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<TopicInfo>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
        assertEquals(1, response.get(0).size());
    }

    //get only topic names
    @Test
    public void test010() throws Exception {
        when(clusterApiService.getAllTopics(anyString(), eq("PLAINTEXT")))
                .thenReturn(utilMethods.getClusterApiTopics("testtopic",10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicsOnly").with(user("gary").password("user"))
                .param("env","DEV")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }


    // Get Acl requests before creating one
    @Test
    public void test01() throws Exception {

        String res = mvc.perform(
                get("/getAclRequests").with(user("gary").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(0, response.size());
    }

    // Get Created Acl requests before creating one
    @Test
    public void test02() throws Exception {

        String res = mvc.perform(get("/getCreatedAclRequests").with(user("gary").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<AclRequests>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(0, response.size());
    }

    // Request for a acl
    @Test
    public void test03() throws Exception {
        AclRequests addAclRequest = utilMethods.getAclRequest11("testtopic");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createAcl").with(user("gary").password("user"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Get created acl requests again
    @Test
    public void test04() throws Exception {

        String res = mvc.perform(get("/getCreatedAclRequests").with(user("gary").password("user"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<AclRequests>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // Get acl requests again, and approve that request
    @Test
    public void test05() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user("gary").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, String> hMap = (LinkedHashMap)obj;
        this.reqId = hMap.get("req_no");

        assertEquals(1, response.size());

        login("cris","user", "ADMIN");

        when(clusterApiService.approveAclRequests(any())).thenReturn(new ResponseEntity<>("success",HttpStatus.OK));

        res = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequest").with(user("cris").password("user").roles("ADMIN"))
                .param("req_no",reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(res, CoreMatchers.containsString("success"));
    }

    // Request for a acl
    @Test
    public void test06() throws Exception {
        AclRequests addAclRequest = utilMethods.getAclRequest("testtopic");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createAcl").with(user("gary").password("user"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Decline acl request
    @Test
    public void test07() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user("gary").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, String> hMap = (LinkedHashMap)obj;
        this.reqId = hMap.get("req_no");

        login("cris","user", "ADMIN");
        String resNew = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequestDecline").with(user("cris").password("user").roles("ADMIN"))
                .param("req_no",reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(resNew, CoreMatchers.containsString("success"));
    }

    // delete acl requests
    @Test
    public void test08() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user("gary").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, String> hMap = (LinkedHashMap)obj;
        this.reqId = hMap.get("req_no");

        String responseNew = mvc.perform(get("/deleteAclRequests").with(user("gary").password("user"))
                .param("req_no",this.reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(responseNew, CoreMatchers.containsString("success"));
    }

    // getacls with topic search filter
    @Test
    public void test11() throws Exception {

        List<HashMap<String,String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls2());

        when(clusterApiService.getAcls(anyString(), eq("PLAINTEXT")))
                .thenReturn(aclInfo);

        String res = mvc.perform(get("/getAcls").with(user("gary").password("user"))
                .param("topicnamesearch","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TopicOverview response = new ObjectMapper().readValue(res, TopicOverview.class);
        assertEquals(1, response.getAclInfoList().size());
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