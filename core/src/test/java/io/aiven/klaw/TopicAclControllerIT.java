package io.aiven.klaw;

import static io.aiven.klaw.helpers.KwConstants.TENANT_CONFIG_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwPropertiesModel;
import io.aiven.klaw.model.ResourceHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.KwClustersModel;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.requests.TopicDeleteRequestModel;
import io.aiven.klaw.model.requests.TopicRequestModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.KwClustersModelResponse;
import io.aiven.klaw.model.response.SchemaOverview;
import io.aiven.klaw.model.response.TeamModelResponse;
import io.aiven.klaw.model.response.TopicOverview;
import io.aiven.klaw.model.response.TopicRequestsResponseModel;
import io.aiven.klaw.service.ClusterApiService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-application-rdbms.properties")
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext
public class TopicAclControllerIT {

  private static final String INFRATEAM = "INFRATEAM";
  private static final String PASSWORD = "user";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static UtilMethods utilMethods;

  private static MockMethods mockMethods;

  @Autowired private MockMvc mvc;

  @MockBean private static ClusterApiService clusterApiService;

  private static final String superAdmin = "superadmin";
  private static final String superAdminPwd = "kwsuperadmin123$$";
  private static final String user1 = "tkwusera", user2 = "tkwuserb", user3 = "tkwuserc";
  private static final String topicName = "testtopic";
  private static final int topicId1 = 1001, topicId3 = 1004, topicId4 = 1006, topicId5 = 1008;

  @BeforeAll
  public static void setup() {
    utilMethods = new UtilMethods();
    mockMethods = new MockMethods();
  }

  // Create user with USER role success
  @Test
  @Order(1)
  public void createRequiredUsers() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel = mockMethods.getUserInfoModel(user1, role, INFRATEAM);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewUser")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});

    assertThat(response1.isSuccess() || response.contains("User already exists")).isTrue();

    userInfoModel = mockMethods.getUserInfoModel(user2, role, INFRATEAM);
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewUser")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});

    assertThat(response1.isSuccess() || response.contains("User already exists")).isTrue();
    userInfoModel = mockMethods.getUserInfoModel(user3, role, INFRATEAM);
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);
    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewUser")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess() || response.contains("User already exists")).isTrue();
  }

  @Test
  @Order(2)
  public void addNewCluster() throws Exception {
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

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

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

    List<KwClustersModelResponse> teamModel =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(teamModel).hasSize(1);
  }

  @Test
  @Order(3)
  public void addNewAivenCluster() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getAivenKafkaClusterModel("DEV_AIVEN_CLUSTER");
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

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

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

    List<KwClustersModelResponse> kwClustersModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(kwClustersModels).hasSize(2);
  }

  @Test
  @Order(4)
  public void createEnv() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("DEV");
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

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

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

  @Test
  @Order(5)
  public void createAivenKafkaEnv() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("DEV_AIVEN");
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

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

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
    assertThat(clusterModels).hasSize(2);
  }

  @Test
  @Order(6)
  public void updateTenantConfig() throws Exception {
    KwPropertiesModel kwPropertiesModel = new KwPropertiesModel();
    kwPropertiesModel.setKwKey(TENANT_CONFIG_PROPERTY);
    kwPropertiesModel.setKwValue(
        """
                    {
                      "tenantModel":
                        {
                          "tenantName": "default",
                          "baseSyncEnvironment": "DEV",
                          "orderOfTopicPromotionEnvsList": ["DEV"],
                          "requestTopicsEnvironmentsList": ["DEV"]
                        }
                    }""");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwPropertiesModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/updateKwCustomProperty")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // Create topic requests
  @Test
  @Order(7)
  public void createTopicRequest() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicId1);
    addTopicRequest.setTopicpartitions(1);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // Query topic requests in created state
  @Test
  @Order(8)
  public void queryTopicRequest() throws Exception {
    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicRequests")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<TopicRequestsResponseModel> response =
        OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(1);
    assertThat(response.get(0).getTopicpartitions()).isEqualTo(1);
  }

  // Create topic requests
  @Test
  @Order(9)
  public void editTopicRequest() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicId1);
    addTopicRequest.setTopicId(1001);
    addTopicRequest.setTopicpartitions(2);
    addTopicRequest.setRequestor(user1);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // Query topic requests in created and approved state
  @Test
  @Order(10)
  public void queryTopicRequestFromId() throws Exception {
    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicRequest")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("topicReqId", "1001")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TopicRequestsResponseModel response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).isNotNull();
    assertThat(response.getTopicpartitions()).isEqualTo(2);
  }

  // approve topic - creates topic in cluster
  @Order(11)
  @Test
  public void approveTopic() throws Exception {
    String topicName = TopicAclControllerIT.topicName + topicId1;
    when(clusterApiService.getClusterApiStatus(anyString(), anyBoolean(), anyInt()))
        .thenReturn(ClusterStatus.ONLINE);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();

    when(clusterApiService.approveTopicRequests(
            topicName, RequestOperationType.CREATE.value, 2, "1", "1", new HashMap<>(), 101, null))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execTopicRequests")
                    .with(user(user2).password(PASSWORD))
                    .param("topicId", topicId1 + "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // decline topic - topic in cluster
  @Order(12)
  @Test
  public void declineTopicRequest() throws Exception {
    int topicIdLocal = 1002;
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicIdLocal);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execTopicRequestsDecline")
                    .with(user(user2).password(PASSWORD))
                    .param("topicId", topicIdLocal + "")
                    .param("reasonForDecline", "reason")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // get team of topic
  @Order(13)
  @Test
  public void getTeamOfTopic() throws Exception {
    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicTeam")
                    .with(user(user1).password(PASSWORD))
                    .param("topicName", topicName + topicId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(res).contains(INFRATEAM);
  }

  // delete a topic request of his own
  @Order(14)
  @Test
  public void deleteTopicRequest() throws Exception {

    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1003);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteTopicRequests")
                    .param("topicId", "1003")
                    .with(user(user1).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // get topics from cluster
  @Order(15)
  @Test
  public void getTopicsFromCluster() throws Exception {
    when(clusterApiService.getAllTopics(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterApiTopics(topicName, 10));

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopics")
                    .with(user(user1).password(PASSWORD))
                    .param("env", "1")
                    .param("pageNo", "1")
                    .param("topicnamesearch", topicName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<List<TopicInfo>> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(1);
    assertThat(response.get(0)).hasSize(1);
  }

  // get only topic names
  @Order(16)
  @Test
  public void getOnlyTopicNames() throws Exception {
    when(clusterApiService.getAllTopics(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString(), anyString(), anyInt()))
        .thenReturn(utilMethods.getClusterApiTopics(topicName, 10));

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicsOnly")
                    .with(user(user1).password(PASSWORD))
                    .param("env", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<String> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(1);
    assertThat(response.get(0)).isEqualTo("testtopic1001");
  }

  // Get Acl requests before creating one
  @Order(17)
  @Test
  public void getAclRequests() throws Exception {

    String res =
        mvc.perform(
                get("/getAclRequests")
                    .with(user(user1).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<AclRequests> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).isEmpty();
  }

  // Get Created Acl requests before creating one
  @Order(18)
  @Test
  public void getCreatedAclReqs() throws Exception {
    String res =
        mvc.perform(
                get("/getAclRequestsForApprover")
                    .with(user(user1).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<List<AclRequests>> response = OBJECT_MAPPER.readValue(res, List.class);
    assertThat(response).isEmpty();
  }

  // Request for a acl
  @Order(19)
  @Test
  public void aclRequest() throws Exception {
    AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName + topicId1);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addAclRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createAcl")
                    .with(user(user1).password(PASSWORD))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // Get created acl requests again
  @Order(20)
  @Test
  public void getCreatedAclRequest() throws Exception {

    String res =
        mvc.perform(
                get("/getAclRequestsForApprover")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<AclRequestsResponseModel> response =
        OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(1);
  }

  // Get acl requests again, and approve that request
  @Order(21)
  @Test
  public void getAclResAgainAndApprove() throws Exception {
    String res =
        mvc.perform(
                get("/getAclRequests")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .param("order", "DESC_REQUESTED_TIME")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<Map<String, Object>> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    Map<String, Object> hMap = response.get(0);

    Map<String, String> dataObj = new HashMap<>();
    ApiResponse apiResponse;
    if (hMap.get("environment").equals("2")) { // Aiven env
      String aivenAclIdKey = "aivenaclid";
      dataObj.put(aivenAclIdKey, "abcdef"); // any test key
      apiResponse =
          ApiResponse.builder()
              .success(true)
              .message(ApiResultStatus.SUCCESS.value)
              .data(dataObj)
              .build();
    } else {
      apiResponse =
          ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
    }

    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    Integer reqNo = (Integer) hMap.get("req_no");

    res =
        mvc.perform(
                MockMvcRequestBuilders.post("/execAclRequest")
                    .with(user(user2).password(PASSWORD))
                    .param("req_no", "" + reqNo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // Request for a acl
  @Order(22)
  @Test
  public void requestAnAcl() throws Exception {
    AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName + topicId1);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addAclRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createAcl")
                    .with(user(user1).password(PASSWORD))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // Decline acl request
  @Order(23)
  @Test
  public void declineAclReq() throws Exception {
    String res =
        mvc.perform(
                get("/getAclRequests")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .param("order", "DESC_REQUESTED_TIME")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<Map<String, Object>> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    Map<String, Object> hMap = response.get(0);
    Integer reqNo = (Integer) hMap.get("req_no");

    String resNew =
        mvc.perform(
                MockMvcRequestBuilders.post("/execAclRequestDecline")
                    .with(user(user1).password(PASSWORD))
                    .param("req_no", "" + reqNo)
                    .param("reasonForDecline", "reason")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(resNew, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // delete acl requests
  @Order(24)
  @Test
  public void deleteAclReq() throws Exception {
    String res =
        mvc.perform(
                get("/getAclRequests")
                    .with(user(user1).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<Map<String, Object>> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    Map<String, Object> hMap = response.get(0);

    String responseNew =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteAclRequests")
                    .with(user(user1).password(PASSWORD))
                    .param("req_no", "" + hMap.get("req_no"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(responseNew, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
  }

  // getacls with topic search filter
  @Order(25)
  @Test
  public void getAclsWithSearch() throws Exception {
    List<Map<String, String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls2());
    when(clusterApiService.getAcls(
            anyString(), any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyInt()))
        .thenReturn(aclInfo);

    String res =
        mvc.perform(
                get("/getTopicOverview")
                    .with(user(user3).password(PASSWORD))
                    .param("topicName", topicName + topicId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TopicOverview response = OBJECT_MAPPER.readValue(res, TopicOverview.class);
    assertThat(response.getAclInfoList()).hasSize(1);
  }
  // get acls to be synced - retrieve from Source of truth
  @Order(26)
  @Test
  public void getAclsToBeSynced() throws Exception {
    List<Map<String, String>> aclInfo = utilMethods.getClusterSyncAcls();

    when(clusterApiService.getAcls(
            anyString(), any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyInt()))
        .thenReturn(aclInfo);

    String res =
        mvc.perform(
                get("/getSyncAcls")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("env", "1")
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<AclInfo> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(2);
    assertThat(response)
        .extracting(AclInfo::getRemarks)
        .containsExactlyInAnyOrder("DELETED", "ADDED");
  }

  // delete acl requests
  @Order(27)
  @Test
  public void deleteAclReqDifferentUseFailsToDeleteRequest() throws Exception {
    String res =
        mvc.perform(
                get("/getAclRequests")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<Map<String, Object>> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    Map<String, Object> hMap = response.get(0);

    String responseNew =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteAclRequests")
                    .with(user(user3).password(PASSWORD))
                    .param("req_no", "" + hMap.get("req_no"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(responseNew).contains(ApiResultStatus.FAILURE.value);
  }

  @Test
  @Order(28)
  public void createDeleteTopicRequest() throws Exception {
    String topicName = createAndApproveTopic(topicId3, false);
    TopicDeleteRequestModel topicDeleteRequestModel = new TopicDeleteRequestModel();
    topicDeleteRequestModel.setTopicName(topicName);
    topicDeleteRequestModel.setEnv("1");
    topicDeleteRequestModel.setDeleteAssociatedSchema(true);

    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(topicDeleteRequestModel);

    // create topic delete request
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopicDeleteRequest")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicRequests")
                    .with(user(user3).password(PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .param("order", "DESC_REQUESTED_TIME")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<TopicRequestsResponseModel> topicRequestModels =
        OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    TopicRequestsResponseModel deleteTopicRequestModel =
        topicRequestModels.stream()
            .filter(topicRequestModel -> topicRequestModel.getTopicname().equals(topicName))
            .findFirst()
            .get();
    assertThat(deleteTopicRequestModel.getDeleteAssociatedSchema()).isTrue();
  }

  @Test
  @Order(29)
  public void createAivenAclRequest() throws Exception {
    String topicName = createAndApproveTopic(topicId4, true);

    AclRequestsModel addAclRequest = utilMethods.getAivenAclRequestModel(topicName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addAclRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createAcl")
                    .with(user(user1).password(PASSWORD))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();
    getAclResAgainAndApprove(); // approve acl request

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTeamDetails")
                    .with(user(superAdmin).password(superAdmin))
                    .param("teamId", "1001")
                    .param("tenantName", "default")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TeamModelResponse teamModel = OBJECT_MAPPER.readValue(response, TeamModelResponse.class);
    assertThat(teamModel.getServiceAccounts()).isNotNull();
    assertThat(teamModel.getServiceAccounts().getServiceAccountsList())
        .contains(addAclRequest.getAcl_ssl().get(0));
  }

  private String createAndApproveTopic(int topicID, boolean aivenEnv) throws Exception {
    // Create a topic
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicID);
    if (aivenEnv) {
      addTopicRequest.setEnvironment("2");
    }
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    mvc.perform(
            MockMvcRequestBuilders.post("/createTopics")
                .with(user(user1).password(PASSWORD).roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // approve the topic
    String topicName = TopicAclControllerIT.topicName + topicID;
    when(clusterApiService.getClusterApiStatus(anyString(), anyBoolean(), anyInt()))
        .thenReturn(ClusterStatus.ONLINE);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();

    when(clusterApiService.approveTopicRequests(
            topicName,
            RequestOperationType.CREATE.value,
            2,
            "1",
            addTopicRequest.getEnvironment(),
            new HashMap<>(),
            101,
            null))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));

    mvc.perform(
            MockMvcRequestBuilders.post("/execTopicRequests")
                .with(user(user3).password(PASSWORD))
                .param("topicId", topicID + "")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    return topicName;
  }

  @Test
  @Order(30)
  public void createEnvWithPrefixAndSuffix() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("TST");
    envModel.getParams().setTopicPrefix(List.of("prefix-"));
    envModel.getParams().setTopicSuffix(List.of("-suffix"));
    envModel.setId("3");
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

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

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
    assertThat(clusterModels).hasSize(3);
  }

  @Test
  @Order(31)
  public void createTopicRequestFailValidation() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicId5);
    addTopicRequest.setTopicname("prefix-t-suffix");
    addTopicRequest.setEnvironment("3");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isFalse();
    assertThat(response1.getMessage())
        .isEqualTo(
            "Topic name: prefix-t-suffix is not long enough when prefix and suffix's are excluded. 3 minimum are required to be unique.");
  }

  @Test
  @Order(32)
  public void createTopicRequestFailValidation_prefix_suffix_overlap() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicId5);
    addTopicRequest.setTopicname("prefix-suffix");
    addTopicRequest.setEnvironment("3");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isFalse();
    assertThat(response1.getMessage())
        .isEqualTo(
            "Topic Suffix and Topic Prefix overlap there is a requirement for prefix-suffix characters minimum to be unique between the prefix and suffix.");
  }

  @Test
  @Order(33)
  public void addNewSRCluster() throws Exception {
    // Schema registry cluster

    KwClustersModel kwClustersModelSch = mockMethods.getSchemaClusterModel("DEV_SCH");
    String jsonReqSch = OBJECT_MAPPER.writer().writeValueAsString(kwClustersModelSch);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewCluster")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReqSch)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response2 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response2.isSuccess()).isTrue();

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getClusters")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("clusterType", KafkaClustersType.SCHEMA_REGISTRY.value)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<KwClustersModelResponse> kwClustersModelResponses =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(kwClustersModelResponses).hasSize(1);
  }

  @Test
  @Order(34)
  public void createSREnv() throws Exception {
    EnvModel envModelSch = mockMethods.getEnvModel("DEVSCH");
    envModelSch.setClusterId(3);
    envModelSch.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
    EnvTag envTag = new EnvTag();
    envTag.setName("SCHD");
    envTag.setId("1");
    envModelSch.setAssociatedEnv(envTag);
    String jsonReqSch = OBJECT_MAPPER.writer().writeValueAsString(envModelSch);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewEnv")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReqSch)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    // get SR envs
    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getSchemaRegEnvs")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<Map<String, Object>> envModels =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(envModels).hasSize(1);
  }

  @Order(35)
  @Test
  public void createSchemaRequest() throws Exception {
    SchemaRequestModel schemaRequest = utilMethods.getSchemaRequests().get(0);
    schemaRequest.setTopicname(topicName + topicId1);
    schemaRequest.setRequestor(user1);
    schemaRequest.setEnvironment("3"); // Schema reg env
    schemaRequest.setSchemafull(
        "{\n"
            + "   \"type\" : \"record\",\n"
            + "   \"namespace\" : \"Klaw\",\n"
            + "   \"name\" : \"Employee\",\n"
            + "   \"fields\" : [\n"
            + "      { \"name\" : \"Name\" , \"type\" : \"string\" },\n"
            + "      { \"name\" : \"Age\" , \"type\" : \"int\" }\n"
            + "   ]\n"
            + "}");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(schemaRequest);
    ApiResponse apiResponse = ApiResponse.builder().success(true).build();
    ResponseEntity<ApiResponse> responseResponseEntity =
        new ResponseEntity<>(apiResponse, HttpStatus.OK);
    when(clusterApiService.validateSchema(anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(responseResponseEntity);
    mvc.perform(
            MockMvcRequestBuilders.post("/uploadSchema")
                .with(user(user1).password(PASSWORD).roles("USER"))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Order(36)
  @Test
  public void execSchemaRequests() throws Exception {
    Map<String, Object> registerSchemaCustomResponse = new HashMap<>();
    registerSchemaCustomResponse.put("schemaRegistered", true);
    registerSchemaCustomResponse.put("version", 1);
    registerSchemaCustomResponse.put("id", 1);
    registerSchemaCustomResponse.put("compatibility", "BACKWARD");

    ApiResponse apiResponse = ApiResponse.builder().data(registerSchemaCustomResponse).build();
    ResponseEntity<ApiResponse> responseResponseEntity =
        new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(responseResponseEntity);
    mvc.perform(
            MockMvcRequestBuilders.post("/execSchemaRequests")
                .with(user(user2).password(PASSWORD).roles("USER"))
                .param("avroSchemaReqId", "1001")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Order(37)
  @Test
  public void getSchemaOverview() throws Exception {
    List<Map<String, String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls2());
    when(clusterApiService.getAcls(
            anyString(), any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyInt()))
        .thenReturn(aclInfo);

    String res =
        mvc.perform(
                get("/getSchemaOfTopic")
                    .with(user(user1).password(PASSWORD))
                    .param("topicName", topicName + topicId1)
                    .param("kafkaEnvId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    SchemaOverview response = OBJECT_MAPPER.readValue(res, SchemaOverview.class);
    assertThat(response.getAllSchemaVersions()).hasSize(1);
  }

  @Order(38)
  @Test
  public void getHistoriesOfTopicAclSchema() throws Exception {
    List<Map<String, String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls2());
    when(clusterApiService.getAcls(
            anyString(), any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyInt()))
        .thenReturn(aclInfo);

    String res =
        mvc.perform(
                get("/getTopicOverview")
                    .with(user(user3).password(PASSWORD))
                    .param("topicName", topicName + topicId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TopicOverview response = OBJECT_MAPPER.readValue(res, TopicOverview.class);
    assertThat(response.getTopicHistoryList())
        .extracting(ResourceHistory::getRemarks)
        .containsExactlyInAnyOrder(
            "TOPIC Create", "ACL Create Consumer - User:*", "SCHEMA Create Version : 1");
  }
}
