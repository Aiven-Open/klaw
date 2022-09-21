package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.KafkaClustersType;
import io.aiven.klaw.model.KwClustersModel;
import io.aiven.klaw.model.KwTenantModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-application-rdbms1.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class EnvsClustersTenantsControllerIT {

  private static MockMethods mockMethods;
  private static final String superAdmin = "superadmin";
  private static final String superAdminPwd = "kwsuperadmin123$$";

  @Autowired private MockMvc mvc;

  @BeforeAll
  public static void setup() {
    mockMethods = new MockMethods();
  }

  // get default tenant success
  @Test
  @Order(1)
  public void getDefaultTenantSuccess() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTenants")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<KwTenantModel> teamModel = new ObjectMapper().readValue(response, List.class);
    assertThat(teamModel).hasSize(1);
  }

  // add tenant success
  @Test
  @Order(2)
  public void addTenantSuccess() throws Exception {
    KwTenantModel kwTenantModel = mockMethods.getTenantModel("nltenant12345678");
    String jsonReq = new ObjectMapper().writer().writeValueAsString(kwTenantModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addTenantId")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTenants")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<KwTenantModel> teamModel = new ObjectMapper().readValue(response, List.class);
    assertThat(teamModel).hasSize(2);
  }

  // add cluster success
  @Test
  @Order(3)
  public void addClusterSuccess() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getClusterModel("DEV_CLUSTER");
    String jsonReq = new ObjectMapper().writer().writeValueAsString(kwClustersModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewCluster")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusters")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterType", KafkaClustersType.KAFKA.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<KwClustersModel> teamModel = new ObjectMapper().readValue(response, List.class);
    assertThat(teamModel).hasSize(1);
  }

  // modify cluster success
  @Test
  @Order(4)
  public void modifyClusterSuccess() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusters")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterType", KafkaClustersType.KAFKA.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List clusterModels = new ObjectMapper().readValue(response, List.class);
    assertThat(clusterModels).hasSize(1);

    Map<String, Integer> linkedHashMap = (Map<String, Integer>) clusterModels.get(0);

    KwClustersModel kwClustersModel = mockMethods.getClusterModel("DEV_CLUSTER");
    kwClustersModel.setClusterId(linkedHashMap.get("clusterId"));
    kwClustersModel.setBootstrapServers("localhost:9093");
    String jsonReq = new ObjectMapper().writer().writeValueAsString(kwClustersModel);
    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewCluster")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");
  }

  // getclusterdetails success
  @Test
  @Order(5)
  public void getClusterDetailsSuccess() throws Exception {

    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusterDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    KwClustersModel clusterModel = new ObjectMapper().readValue(response, KwClustersModel.class);
    assertThat(clusterModel.getClusterName()).contains("DEV_CLUSTER");
  }

  // add cluster and delete success
  @Test
  @Order(6)
  public void addAndDeleteClusterSuccess() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getClusterModel("TST_CLUSTER");
    String jsonReq = new ObjectMapper().writer().writeValueAsString(kwClustersModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewCluster")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusters")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterType", KafkaClustersType.KAFKA.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List clusterModels = new ObjectMapper().readValue(response, List.class);
    assertThat(clusterModels).hasSize(2);

    Map<String, Integer> linkedHashMap = (Map) clusterModels.get(0);

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteCluster")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterId", "" + linkedHashMap.get("clusterId"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusters")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterType", KafkaClustersType.KAFKA.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    clusterModels = new ObjectMapper().readValue(response, List.class);
    assertThat(clusterModels).hasSize(1);
  }

  // add new env success
  @Test
  @Order(7)
  public void addNewEnvSuccess() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("DEV");
    envModel.setClusterId(2);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(envModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewEnv")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getEnvs")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List clusterModels = new ObjectMapper().readValue(response, List.class);
    assertThat(clusterModels).hasSize(1);
  }

  // modify env success
  @Test
  @Order(8)
  public void modifyEnvSuccess() throws Exception {

    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getEnvs")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List clusterModels = new ObjectMapper().readValue(response, List.class);
    Map<String, Object> envModel1 = (Map<String, Object>) clusterModels.get(0);
    String envId = (String) envModel1.get("id");
    assertThat(clusterModels).hasSize(1);

    String otherParams =
        "default.partitions=4,max.partitions=2,replication.factor=1,topic.prefix=,topic.suffix=";
    EnvModel envModel = mockMethods.getEnvModel("DEV");
    envModel.setId(envId);
    envModel.setClusterId(2);
    envModel.setOtherParams(otherParams);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(envModel);

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewEnv")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getEnvs")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    clusterModels = new ObjectMapper().readValue(response, List.class);
    Map<String, Object> envModel3 = (Map<String, Object>) clusterModels.get(0);
    String updatedOtherParams = (String) envModel3.get("otherParams");
    assertThat(updatedOtherParams).isEqualTo(otherParams);
  }

  // get envdetails success
  @Test
  @Order(9)
  public void getEnvDetailsSuccess() throws Exception {

    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getEnvDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("envSelected", "1")
                    .param("envType", KafkaClustersType.KAFKA.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    EnvModel envModel = new ObjectMapper().readValue(response, EnvModel.class);
    assertThat(envModel.getName()).isEqualTo("DEV");
  }

  // delete env success
  @Test
  @Order(10)
  public void deleteEnvSuccess() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("ACC");
    String jsonReq = new ObjectMapper().writer().writeValueAsString(envModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewEnv")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getEnvs")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List clusterModels = new ObjectMapper().readValue(response, List.class);
    assertThat(clusterModels).hasSize(1);

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteEnvironmentRequest")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("envId", "2")
                    .param("envType", KafkaClustersType.KAFKA.value)
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");
  }

  // get env params success
  @Test
  @Order(11)
  public void getEnvParamsSuccess() throws Exception {

    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getEnvParams")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("envSelected", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    HashMap clusterModels = new ObjectMapper().readValue(response, HashMap.class);
    ArrayList<String> defaultPartitions = (ArrayList) clusterModels.get("defaultPartitions");
    assertThat(defaultPartitions.get(0)).isEqualTo("4");
  }

  // get standard env names success
  @Test
  @Order(12)
  public void getStandardEnvNames() throws Exception {

    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getStandardEnvNames")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List clusterModels = new ObjectMapper().readValue(response, List.class);
    assertThat(clusterModels.get(0)).isEqualTo("ACC");
  }
}
