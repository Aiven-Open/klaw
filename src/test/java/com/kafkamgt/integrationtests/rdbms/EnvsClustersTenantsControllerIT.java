package com.kafkamgt.integrationtests.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.integrationtests.rdbms.utils.MockMethods;
import com.kafkamgt.uiapi.UiapiApplication;
import com.kafkamgt.uiapi.model.*;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes=UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations="classpath:test-application-rdbms1.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class EnvsClustersTenantsControllerIT {

    private static MockMethods mockMethods;
    private static final String superAdmin = "superadmin";
    private static final String superAdminPwd = "kwsuperadmin123$$";

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setup() {
        mockMethods = new MockMethods();
    }

    // get default tenant success
    @Test
    @Order(1)
    public void getDefaultTenantSuccess() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getTenants").with(user(superAdmin).password(superAdminPwd))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<KwTenantModel> teamModel = new ObjectMapper().readValue(response, List.class);
        assertEquals(1, teamModel.size());
    }

    // add tenant success
    @Test
    @Order(2)
    public void addTenantSuccess() throws Exception {
        KwTenantModel kwTenantModel = mockMethods.getTenantModel("nltenant12345678");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(kwTenantModel);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addTenantId").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getTenants").with(user(superAdmin).password(superAdminPwd))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<KwTenantModel> teamModel = new ObjectMapper().readValue(response, List.class);
        assertEquals(2, teamModel.size());
    }

    // add cluster success
    @Test
    @Order(3)
    public void addClusterSuccess() throws Exception {
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

    // modify cluster success
    @Test
    @Order(4)
    public void modifyClusterSuccess() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getClusters").with(user(superAdmin).password(superAdminPwd))
                .param("clusterType","kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List clusterModels = new ObjectMapper().readValue(response, List.class);
        assertEquals(1, clusterModels.size());

        LinkedHashMap<String, Integer> linkedHashMap = (LinkedHashMap)clusterModels.get(0);

        KwClustersModel kwClustersModel = mockMethods.getClusterModel("DEV_CLUSTER");
        kwClustersModel.setClusterId(linkedHashMap.get("clusterId"));
        kwClustersModel.setBootstrapServers("localhost:9093");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(kwClustersModel);
        response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewCluster").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // getclusterdetails success
    @Test
    @Order(5)
    public void getClusterDetailsSuccess() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getClusterDetails").with(user(superAdmin).password(superAdminPwd))
                .param("clusterId","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        KwClustersModel clusterModel = new ObjectMapper().readValue(response, KwClustersModel.class);
        assertEquals("DEV_CLUSTER", clusterModel.getClusterName());
    }

    // add cluster and delete success
    @Test
    @Order(6)
    public void addAndDeleteClusterSuccess() throws Exception {
        KwClustersModel kwClustersModel = mockMethods.getClusterModel("TST_CLUSTER");
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

        List clusterModels = new ObjectMapper().readValue(response, List.class);
        assertEquals(2, clusterModels.size());

        LinkedHashMap<String, Integer> linkedHashMap = (LinkedHashMap)clusterModels.get(0);

        response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteCluster").with(user(superAdmin).password(superAdminPwd))
                .param("clusterId",""+linkedHashMap.get("clusterId"))
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

        clusterModels = new ObjectMapper().readValue(response, List.class);
        assertEquals(1, clusterModels.size());
    }

    // add new env success
    @Test
    @Order(7)
    public void addNewEnvSuccess() throws Exception {
        EnvModel envModel = mockMethods.getEnvModel("DEV");
        envModel.setClusterId(2);
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

    // modify env success
    @Test
    @Order(8)
    public void modifyEnvSuccess() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getEnvs").with(user(superAdmin).password(superAdminPwd))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List clusterModels = new ObjectMapper().readValue(response, List.class);
        HashMap<String, Object> envModel1 = (HashMap<String, Object>)clusterModels.get(0);
        String envId = (String) envModel1.get("id");
        assertEquals(1, clusterModels.size());

        String otherParams = "default.partitions=4,max.partitions=2,replication.factor=1,topic.prefix=,topic.suffix=";
        EnvModel envModel = mockMethods.getEnvModel("DEV");
        envModel.setId(envId);
        envModel.setClusterId(2);
        envModel.setOtherParams(otherParams);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(envModel);

        response = mvc.perform(MockMvcRequestBuilders
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

        clusterModels = new ObjectMapper().readValue(response, List.class);
        HashMap<String, Object> envModel3 = (HashMap<String, Object>)clusterModels.get(0);
        String updatedOtherParams = (String)envModel3.get("otherParams");
        assertEquals(otherParams, updatedOtherParams);
    }

    // get envdetails success
    @Test
    @Order(9)
    public void getEnvDetailsSuccess() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getEnvDetails").with(user(superAdmin).password(superAdminPwd))
                .param("envSelected","1")
                .param("envType","kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        EnvModel envModel = new ObjectMapper().readValue(response, EnvModel.class);
        assertEquals("DEV", envModel.getName());
    }

    // delete env success
    @Test
    @Order(10)
    public void deleteEnvSuccess() throws Exception {
        EnvModel envModel = mockMethods.getEnvModel("ACC");
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

        response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteEnvironmentRequest").with(user(superAdmin).password(superAdminPwd))
                .param("envId","2")
                .param("envType","kafka")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    // get env params success
    @Test
    @Order(11)
    public void getEnvParamsSuccess() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getEnvParams").with(user(superAdmin).password(superAdminPwd))
                .param("envSelected","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        HashMap clusterModels = new ObjectMapper().readValue(response, HashMap.class);
        ArrayList<String> defaultPartitions = (ArrayList)clusterModels.get("defaultPartitions");
        assertEquals("4", defaultPartitions.get(0));
    }

    // get standard env names success
    @Test
    @Order(12)
    public void getStandardEnvNames() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getStandardEnvNames").with(user(superAdmin).password(superAdminPwd))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List clusterModels = new ObjectMapper().readValue(response, List.class);
        assertEquals("ACC", clusterModels.get(0));
    }

}