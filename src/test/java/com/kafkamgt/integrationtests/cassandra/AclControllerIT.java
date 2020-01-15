package com.kafkamgt.integrationtests.cassandra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UiapiApplication;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.model.AclInfo;
import com.kafkamgt.uiapi.service.ClusterApiService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes=UiapiApplication.class)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations="classpath:test-application-cassandra.properties")
@DirtiesContext
@EnableAsync
public class AclControllerIT {

    private UtilMethods utilMethods;

    private MockMvc mvc;

    private String reqId = null;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ClusterApiService clusterApiService;

    @BeforeClass
    public static void init(){
        UtilMethods.startEmbeddedCassandraServer();
    }

    @Before
    public void setup() throws Exception {
        utilMethods = new UtilMethods();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void getClusterApiStatus() throws Exception {
//        this.testRestTemplate = new TestRestTemplate("uiuser1","user");
//        ResponseEntity<String> t = testRestTemplate.getForEntity("http://localhost:"+port+"/kafkawize" +
//                "/getClusterApiStatus", String.class);
//        Env jsonReq = new ObjectMapper().readValue(t.getBody(), Env.class);
//        assertEquals("OFFLINE", jsonReq.getEnvStatus());
    }

    @Test
    public void getEnvsStatus() throws Exception {
//        String response = mvc.perform(get("/getClusterApiStatus")
//                .with(user("uiuser1").password("user")))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        Env jsonReq = new ObjectMapper().readValue(response, Env.class);
//        assertEquals("OFFLINE", jsonReq.getEnvStatus());
    }

    // Get Acl requests before creating one
    @Test
    public void test01() throws Exception {

        String res = mvc.perform(
                get("/getAclRequests").with(user("uiuser1").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(0, response.size());
    }

    // Get Created Acl requests before creating one
    @Test
    public void test02() throws Exception {

        String res = mvc.perform(get("/getCreatedAclRequests").with(user("uiuser1").password("user"))
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
        AclRequests addAclRequest = utilMethods.getAclRequest11("testtopic1");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createAcl").with(user("uiuser1").password("user"))
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

        String res = mvc.perform(get("/getCreatedAclRequests").with(user("uiuser1").password("user"))
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
                get("/getAclRequests").with(user("uiuser1").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, String> hMap = (LinkedHashMap)obj;
        this.reqId = hMap.get("req_no");

        assertEquals(1, response.size());

        login("uiuser4","user", "ADMIN");

        when(clusterApiService.approveAclRequests(any())).thenReturn(new ResponseEntity<>("success",HttpStatus.OK));

        res = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequest").with(user("uiuser4").password("user").roles("ADMIN"))
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
                .post("/createAcl").with(user("uiuser1").password("user"))
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
                get("/getAclRequests").with(user("uiuser1").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, String> hMap = (LinkedHashMap)obj;
        this.reqId = hMap.get("req_no");

        login("uiuser4","user", "ADMIN");
        String resNew = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequestDecline").with(user("uiuser4").password("user").roles("ADMIN"))
                .param("req_no",reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(resNew, CoreMatchers.containsString("success"));
    }

    // delete acl requests
    @Test
    public void test8() throws Exception {
        String res = mvc.perform(
                get("/getAclRequests").with(user("uiuser1").password("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        Object obj = response.get(0);
        LinkedHashMap<String, String> hMap = (LinkedHashMap)obj;
        this.reqId = hMap.get("req_no");

        String responseNew = mvc.perform(get("/deleteAclRequests").with(user("uiuser1").password("user"))
                .param("req_no",this.reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(responseNew, CoreMatchers.containsString("success"));
    }

    // update acls with team - sync
    @Test
    public void test9() throws Exception {

        String topicName = "testtopic";
        String updateSyncAcls = topicName + "-----" + "Team1" + "-----"
                + "testconsumergroup" + "-----" + "10.11.11.223" + "-----"+null+"-----"+"consumer"+"\n";
        String envSelected = "DEV";

        login("superuser","user", "SUPERUSER");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/updateSyncAcls").with(user("superuser").password("user").roles("SUPERUSER"))
                .param("updatedSyncAcls",updateSyncAcls)
                .param("envSelected",envSelected)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // getacls with topic search filter
    @Test
    public void test11() throws Exception {

        List<HashMap<String,String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls());

        when(clusterApiService.getAcls(anyString()))
                .thenReturn(aclInfo);

        String res = mvc.perform(get("/getAcls").with(user("uiuser1").password("user"))
                .param("env","DEV")
                .param("pageNo","1")
                .param("topicnamesearch","testtopic1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclInfo> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    // get acls to be synced - retrieve from Source of truth
    @Test
    public void test13() throws Exception {
        List<HashMap<String,String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls());

        when(clusterApiService.getAcls(anyString()))
                .thenReturn(aclInfo);

        String res = mvc.perform(get("/getSyncAcls").with(user("uiuser1").password("user"))
                .param("env","DEV")
                .param("pageNo","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclInfo> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(2, response.size());
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