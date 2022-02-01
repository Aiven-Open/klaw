package com.kafkamgt.integrationtests.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.integrationtests.rdbms.utils.MockMethods;
import com.kafkamgt.uiapi.UiapiApplication;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.model.*;
import com.kafkamgt.uiapi.service.ClusterApiService;
import org.hamcrest.CoreMatchers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.kafkamgt.uiapi.service.KwConstants.TENANT_CONFIG_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes=UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations="classpath:test-application-rdbms.properties")
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext
public class TopicAclControllerIT {

    private static final String INFRATEAM = "INFRATEAM";
    private static final String PASSWORD = "user";
    private static UtilMethods utilMethods;

    private static MockMethods mockMethods;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private static ClusterApiService clusterApiService;

    private static final String superAdmin = "superadmin";
    private static final String superAdminPwd = "kwsuperadmin123$$";
    private static final String user1 = "kwusera", user2="kwuserb";
    private static final String topicName = "testtopic";
    private static final int topicId = 1001;

    @BeforeAll
    public static void setup(){
        utilMethods = new UtilMethods();
        mockMethods = new MockMethods();
    }

    // Create user with USER role success
    @Test
    @Order(1)
    public void createRequiredUsers() throws Exception {
        String role = "USER";
        UserInfoModel userInfoModel = mockMethods.getUserInfoModel(user1, role, INFRATEAM);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(userInfoModel);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewUser").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        userInfoModel = mockMethods.getUserInfoModel(user2, role, "INFRATEAM");
        jsonReq = new ObjectMapper().writer().writeValueAsString(userInfoModel);

        response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewUser").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getUserDetails").with(user(superAdmin).password(superAdminPwd))
                .param("userId",user1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserInfoModel userInfoModel1 = new ObjectMapper().readValue(response, UserInfoModel.class);
        assertEquals("INFRATEAM", userInfoModel1.getTeam());
        assertEquals(user1, userInfoModel1.getUsername());
        assertEquals(role, userInfoModel1.getRole());
    }


    @Test
    @Order(2)
    public void addNewCluster() throws Exception {
        KwClustersModel kwClustersModel = mockMethods.getClusterModel("DEV_CLUSTER");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(kwClustersModel);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewCluster").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getClusters").with(user(superAdmin).password(superAdminPwd))
                .param("clusterType","kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<KwClustersModel> teamModel = new ObjectMapper().readValue(response, List.class);
        assertEquals(1, teamModel.size());
    }

    @Test
    @Order(3)
    public void createEnv() throws Exception {
        EnvModel envModel = mockMethods.getEnvModel("DEV");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(envModel);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewEnv").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getEnvs").with(user(superAdmin).password(superAdminPwd))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List clusterModels = new ObjectMapper().readValue(response, List.class);
        assertEquals(1, clusterModels.size());
    }

    @Test
    @Order(4)
    public void updateTenantConfig() throws Exception {
        KwPropertiesModel kwPropertiesModel = new KwPropertiesModel();
        kwPropertiesModel.setKwKey(TENANT_CONFIG_PROPERTY);
        kwPropertiesModel.setKwValue("{\n" +
                "  \"tenantModel\":\n" +
                "    {\n" +
                "      \"tenantName\": \"default\",\n" +
                "      \"baseSyncEnvironment\": \"DEV\",\n" +
                "      \"orderOfTopicPromotionEnvsList\": [\"DEV\"],\n" +
                "      \"requestTopicsEnvironmentsList\": [\"DEV\"]\n" +
                "    }\n" +
                "}");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(kwPropertiesModel);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/updateKwCustomProperty").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Create topic requests
    @Test
    @Order(5)
    public void createTopicRequest() throws Exception {
        TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(topicId);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        login(user1,PASSWORD, "USER");
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user(user1).password(PASSWORD).roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Query topic requests in created state
    @Test
    @Order(6)
    public void queryTopicRequest() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getCreatedTopicRequests").with(user(user1).password(PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // Query topic requests in created and approved state
    @Test
    @Order(7)
    public void queryTopicRequestInCreatedApprovedState() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicRequests").with(user(user1).password(PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // approve topic - creates topic in cluster
    @Order(8)
    @Test
    public void approveTopic() throws Exception {
        String topicName = this.topicName + topicId;
        when(clusterApiService.getClusterApiStatus(anyString(), anyBoolean(), anyInt()))
                .thenReturn("ONLINE");

        when(clusterApiService.approveTopicRequests(topicName,
                "Create", 2, "1", "1", 101))
                .thenReturn(new ResponseEntity<>("success",HttpStatus.OK));

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequests").with(user(user2).password(PASSWORD))
                .param("topicId",topicId + "")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // decline topic - topic in cluster
    @Order(9)
    @Test
    public void declineTopicRequest() throws Exception {
        int topicIdLocal = 1002;
        TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(topicIdLocal);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        login(user1,PASSWORD, "USER");
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user(user1).password(PASSWORD).roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        login(user2,PASSWORD, "APPROVER");

        response = mvc.perform(MockMvcRequestBuilders
                .post("/execTopicRequestsDecline").with(user(user2).password(PASSWORD))
                .param("topicId",topicIdLocal + "")
                .param("reasonForDecline","reason")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // get team of topic
    @Order(10)
    @Test
    public void getTeamOfTopic() throws Exception {
        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicTeam").with(user(user1).password(PASSWORD))
                .param("topicName",topicName+topicId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(res, CoreMatchers.containsString(INFRATEAM));
    }

    // delete a topic request of his own
    @Order(11)
    @Test
    public void deleteTopicRequest() throws Exception {

        TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1003);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
        login(user1,PASSWORD, "USER");
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createTopics").with(user(user1).password(PASSWORD).roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteTopicRequests")
                .param("topicId","1003")
                .with(user(user1).password(PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // get topics from cluster
    @Order(12)
    @Test
    public void getTopicsFromCluster() throws Exception {
        when(clusterApiService.getAllTopics(anyString(), eq("PLAINTEXT"), anyString(), anyInt()))
                .thenReturn(utilMethods.getClusterApiTopics(topicName,10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopics").with(user(user1).password(PASSWORD))
                .param("env","1")
                .param("pageNo","1")
                .param("topicnamesearch",topicName)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<TopicInfo>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
        assertEquals(1, response.get(0).size());
    }

    //get only topic names
    @Order(13)
    @Test
    public void getOnlyTopicNames() throws Exception {
        when(clusterApiService.getAllTopics(anyString(), eq("PLAINTEXT"), anyString(), anyInt()))
                .thenReturn(utilMethods.getClusterApiTopics(topicName,10));

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getTopicsOnly").with(user(user1).password(PASSWORD))
                .param("env","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
        assertEquals("testtopic1001", response.get(0));
    }

    // Get Acl requests before creating one
    @Order(14)
    @Test
    public void getAclRequests() throws Exception {

        String res = mvc.perform(
                get("/getAclRequests").with(user(user1).password(PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(0, response.size());
    }

    // Get Created Acl requests before creating one
    @Order(15)
    @Test
    public void getCreatedAclReqs() throws Exception {
        String res = mvc.perform(get("/getCreatedAclRequests").with(user(user1).password(PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<AclRequests>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(0, response.size());
    }

    // Request for a acl
    @Order(16)
    @Test
    public void aclRequest() throws Exception {
        AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName + topicId);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createAcl").with(user(user1).password(PASSWORD))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Get created acl requests again
    @Order(17)
    @Test
    public void getCreatedAclRequest() throws Exception {

        String res = mvc.perform(get("/getCreatedAclRequests").with(user(user1).password(PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<AclRequests>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // Get acl requests again, and approve that request
    @Order(18)
    @Test
    public void getAclResAgainAndApprove() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user(user1).password(PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, Integer> hMap = (LinkedHashMap)obj;

        when(clusterApiService.approveAclRequests(any(), anyInt())).thenReturn(new ResponseEntity<>("success",HttpStatus.OK));
        Integer reqNo = hMap.get("req_no");

        res = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequest").with(user(user2).password(PASSWORD))
                .param("req_no", ""+reqNo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(res, CoreMatchers.containsString("success"));
    }

    // Request for a acl
    @Order(19)
    @Test
    public void requestAnAcl() throws Exception {
        AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName+topicId);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createAcl").with(user(user1).password(PASSWORD))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Decline acl request
    @Order(20)
    @Test
    public void declineAclReq() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user(user1).password(PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, Integer> hMap = (LinkedHashMap)obj;
        Integer reqNo = hMap.get("req_no");

        String resNew = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequestDecline").with(user(user2).password(PASSWORD))
                .param("req_no",""+reqNo)
                .param("reasonForDecline","reason")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(resNew, CoreMatchers.containsString("success"));
    }

    // delete acl requests
    @Order(21)
    @Test
    public void deleteAclReq() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user(user1).password(PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNo","1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, Integer> hMap = (LinkedHashMap)obj;

        String responseNew = mvc.perform(MockMvcRequestBuilders
                .post("/deleteAclRequests").with(user(user1).password(PASSWORD))
                .param("req_no", "" + hMap.get("req_no"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(responseNew, CoreMatchers.containsString("success"));
    }

    // getacls with topic search filter
    @Order(22)
    @Test
    public void getAclsWithSearch() throws Exception {
        List<HashMap<String,String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls2());
        when(clusterApiService.getAcls(anyString(), eq("PLAINTEXT"), anyString(), anyInt()))
                .thenReturn(aclInfo);

        String res = mvc.perform(get("/getAcls").with(user(user1).password(PASSWORD))
                .param("topicnamesearch",topicName+topicId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TopicOverview response = new ObjectMapper().readValue(res, TopicOverview.class);
        assertEquals(1, response.getAclInfoList().size());
    }

    // get acls to be synced - retrieve from Source of truth
    @Order(23)
    @Test
    public void getAclsToBeSynced() throws Exception {
        List<HashMap<String,String>> aclInfo = utilMethods.getClusterAcls();

        when(clusterApiService.getAcls(anyString(), eq("PLAINTEXT"), anyString(), anyInt()))
                .thenReturn(aclInfo);

        String res = mvc.perform(get("/getSyncAcls").with(user(superAdmin).password(superAdminPwd))
                .param("env","1")
                .param("pageNo","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclInfo> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    private void login(String user, String pwd, String role) throws Exception {
        mvc.perform(get("/login").with(user(user).password(pwd).roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}