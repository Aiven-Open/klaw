package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.KwClustersModel;
import io.aiven.klaw.model.KwTenantModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
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

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

    List<KwTenantModel> teamModel = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(teamModel).hasSize(1);
  }

  // add tenant success
  @Test
  @Order(2)
  public void addTenantSuccess() throws Exception {
    KwTenantModel kwTenantModel = mockMethods.getTenantModel("nltenant12345678");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwTenantModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<KwTenantModel> teamModel = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(teamModel).hasSize(2);
  }

  // add cluster success
  @Test
  @Order(3)
  public void addKafkaClusterSuccess() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getKafkaClusterModel("DEV_CLUSTER");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwClustersModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<KwClustersModel> teamModel = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
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

    List<Map<String, Object>> clusterModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(clusterModels).hasSize(1);

    Map<String, Object> hashMap = clusterModels.get(0);
    KwClustersModel kwClustersModel = mockMethods.getKafkaClusterModel("DEV_CLUSTER");
    kwClustersModel.setClusterId((Integer) hashMap.get("clusterId"));
    kwClustersModel.setBootstrapServers("localhost:9093");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwClustersModel);
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
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

    KwClustersModel clusterModel = OBJECT_MAPPER.readValue(response, KwClustersModel.class);
    assertThat(clusterModel.getClusterName()).contains("DEV_CLUSTER");
  }

  // add cluster and delete success
  @Test
  @Order(6)
  public void addAndDeleteClusterSuccess() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getKafkaClusterModel("TST_CLUSTER");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwClustersModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<Map<String, Object>> clusterModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(clusterModels).hasSize(2);

    Map<String, Object> hashMap = clusterModels.get(0);
    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteCluster")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterId", "" + hashMap.get("clusterId"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    clusterModels = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(clusterModels).hasSize(1);
  }

  // add new env success
  @Test
  @Order(7)
  public void addNewEnvSuccess() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("DEV");
    envModel.setClusterId(2);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(envModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<Map<String, Object>> clusterModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
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

    List<Map<String, Object>> clusterModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    Map<String, Object> envModel1 = clusterModels.get(0);
    String envId = (String) envModel1.get("id");
    assertThat(clusterModels).hasSize(1);

    String otherParams =
        "default.partitions=4,max.partitions=2,replication.factor=1,topic.prefix=,topic.suffix=";
    EnvModel envModel = mockMethods.getEnvModel("DEV");
    envModel.setId(envId);
    envModel.setClusterId(2);
    envModel.setOtherParams(otherParams);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(envModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    clusterModels = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    Map<String, Object> envModel3 = clusterModels.get(0);
    String updatedOtherParams = (String) envModel3.get("otherParams");
    assertThat(updatedOtherParams).isEqualTo(otherParams);
  }

  // modify env failure, submit already existing name TST
  @Test
  @Order(9)
  public void modifyEnvFailureSameName() throws Exception {
    // Add a new env PRD
    EnvModel envModel1 = mockMethods.getEnvModel("PRD");
    envModel1.setClusterId(2);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(envModel1);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<Map<String, Object>> envModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    Map<String, Object> envModel2 = new HashMap<>();
    for (Map<String, Object> envModel : envModels) {
      if (envModel.get("id").equals("2")) { // PRD id
        envModel2 = envModel;
      }
    }
    String envId = (String) envModel2.get("id");
    assertThat(envModels).hasSize(2);

    String otherParams =
        "default.partitions=4,max.partitions=2,replication.factor=1,topic.prefix=,topic.suffix=";
    EnvModel envModel = mockMethods.getEnvModel("PRD");
    envModel.setName("DEV"); // DEV env already exists, fail
    envModel.setId(envId);
    envModel.setClusterId(2);
    envModel.setOtherParams(otherParams);
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(envModel);

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

    assertThat(response)
        .contains("Failure. Please choose a different name. This environment name already exists.");

    // delete the environment PRD
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
  }

  // get envdetails success
  @Test
  @Order(10)
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

    EnvModel envModel = OBJECT_MAPPER.readValue(response, EnvModel.class);
    assertThat(envModel.getName()).isEqualTo("DEV");
  }

  // delete env success
  @Test
  @Order(11)
  public void deleteEnvSuccess() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("ACC");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(envModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<Map<String, Object>> clusterModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // get env params success
  @Test
  @Order(12)
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

    HashMap<String, ArrayList<String>> clusterModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    ArrayList<String> defaultPartitions = clusterModels.get("defaultPartitions");
    assertThat(defaultPartitions.get(0)).isEqualTo("4");
  }

  // get standard env names success
  @Test
  @Order(13)
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

    List<String> clusterModels = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(clusterModels.get(0)).isEqualTo("ACC");
  }

  // add kafka rest cluster success
  @Test
  @Order(14)
  public void addKafkaRestApiClusterSuccess() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getKafkaRestClusterModel("DEV_CLUSTER");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwClustersModel);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusters")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterType", KafkaClustersType.KAFKA_REST_API.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<KwClustersModel> kwClustersModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(kwClustersModels).hasSize(1);
  }
}
