package io.aiven.klaw;

import static io.aiven.klaw.helpers.KwConstants.TENANT_CONFIG_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.KafkaSupportedProtocol;
import io.aiven.klaw.model.KwClustersModel;
import io.aiven.klaw.model.KwPropertiesModel;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicOverview;
import io.aiven.klaw.model.UserInfoModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.requests.TopicRequestModel;
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
  private static final int topicId1 = 1001, topicId3 = 1004;

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

    assertThat(
            response.contains(ApiResultStatus.SUCCESS.value)
                || response.contains("User already exists"))
        .isTrue();

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

    assertThat(
            response.contains(ApiResultStatus.SUCCESS.value)
                || response.contains("User already exists"))
        .isTrue();
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
    assertThat(
            response.contains(ApiResultStatus.SUCCESS.value)
                || response.contains("User already exists"))
        .isTrue();
  }

  @Test
  @Order(2)
  public void addNewCluster() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getClusterModel("DEV_CLUSTER");
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

    List<KwClustersModel> teamModel = OBJECT_MAPPER.readValue(response, List.class);
    assertThat(teamModel).hasSize(1);
  }

  @Test
  @Order(3)
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

  @Test
  @Order(4)
  public void updateTenantConfig() throws Exception {
    KwPropertiesModel kwPropertiesModel = new KwPropertiesModel();
    kwPropertiesModel.setKwKey(TENANT_CONFIG_PROPERTY);
    kwPropertiesModel.setKwValue(
        "{\n"
            + "  \"tenantModel\":\n"
            + "    {\n"
            + "      \"tenantName\": \"default\",\n"
            + "      \"baseSyncEnvironment\": \"DEV\",\n"
            + "      \"orderOfTopicPromotionEnvsList\": [\"DEV\"],\n"
            + "      \"requestTopicsEnvironmentsList\": [\"DEV\"]\n"
            + "    }\n"
            + "}");
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // Create topic requests
  @Test
  @Order(5)
  public void createTopicRequest() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicId1);
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // Query topic requests in created state
  @Test
  @Order(6)
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

    List<TopicRequestModel> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(1);
  }

  // Query topic requests in created and approved state
  @Test
  @Order(7)
  public void queryTopicRequestInCreatedApprovedState() throws Exception {
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

    List<TopicRequestModel> response = OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    assertThat(response).hasSize(1);
  }

  // approve topic - creates topic in cluster
  @Order(8)
  @Test
  public void approveTopic() throws Exception {
    String topicName = TopicAclControllerIT.topicName + topicId1;
    when(clusterApiService.getClusterApiStatus(anyString(), anyBoolean(), anyInt()))
        .thenReturn("ONLINE");
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // decline topic - topic in cluster
  @Order(9)
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // get team of topic
  @Order(10)
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
  @Order(11)
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // get topics from cluster
  @Order(12)
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
  @Order(13)
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

    List<String> response = OBJECT_MAPPER.readValue(res, List.class);
    assertThat(response).hasSize(1);
    assertThat(response.get(0)).isEqualTo("testtopic1001");
  }

  // Get Acl requests before creating one
  @Order(14)
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

    List<AclRequests> response = OBJECT_MAPPER.readValue(res, List.class);
    assertThat(response).isEmpty();
  }

  // Get Created Acl requests before creating one
  @Order(15)
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
  @Order(16)
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // Get created acl requests again
  @Order(17)
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

    List<List<AclRequests>> response = OBJECT_MAPPER.readValue(res, List.class);
    assertThat(response).hasSize(1);
  }

  // Get acl requests again, and approve that request
  @Order(18)
  @Test
  public void getAclResAgainAndApprove() throws Exception {
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

    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
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

    assertThat(res).contains(ApiResultStatus.SUCCESS.value);
  }

  // Request for a acl
  @Order(19)
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  // Decline acl request
  @Order(20)
  @Test
  public void declineAclReq() throws Exception {
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
    Integer reqNo = (Integer) hMap.get("req_no");

    String resNew =
        mvc.perform(
                MockMvcRequestBuilders.post("/execAclRequestDecline")
                    .with(user(user2).password(PASSWORD))
                    .param("req_no", "" + reqNo)
                    .param("reasonForDecline", "reason")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(resNew).contains(ApiResultStatus.SUCCESS.value);
  }

  // delete acl requests
  @Order(21)
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

    assertThat(responseNew).contains(ApiResultStatus.SUCCESS.value);
  }

  // getacls with topic search filter
  @Order(22)
  @Test
  public void getAclsWithSearch() throws Exception {
    List<Map<String, String>> aclInfo = new ArrayList<>(utilMethods.getClusterAcls2());
    when(clusterApiService.getAcls(
            anyString(), any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyInt()))
        .thenReturn(aclInfo);

    String res =
        mvc.perform(
                get("/getAcls")
                    .with(user(user3).password(PASSWORD))
                    .param("topicnamesearch", topicName + topicId1)
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
  @Order(23)
  @Test
  public void getAclsToBeSynced() throws Exception {
    List<Map<String, String>> aclInfo = utilMethods.getClusterAcls();

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
    assertThat(response).hasSize(1);
  }

  // delete acl requests
  @Order(24)
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
  @Order(25)
  public void createDeleteTopicRequest() throws Exception {
    String topicName = createAndApproveTopic();

    // create topic delete request
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopicDeleteRequest")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .param("topicName", topicName)
                    .param("env", "1")
                    .param("deleteAssociatedSchema", "true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<TopicRequestModel> topicRequestModels =
        OBJECT_MAPPER.readValue(res, new TypeReference<>() {});
    TopicRequestModel deleteTopicRequestModel =
        topicRequestModels.stream()
            .filter(topicRequestModel -> topicRequestModel.getTopicname().equals(topicName))
            .findFirst()
            .get();
    assertThat(deleteTopicRequestModel.getDeleteAssociatedSchema()).isTrue();
  }

  private String createAndApproveTopic() throws Exception {
    // Create a topic
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(topicId3);
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
    String topicName = TopicAclControllerIT.topicName + topicId3;
    when(clusterApiService.getClusterApiStatus(anyString(), anyBoolean(), anyInt()))
        .thenReturn("ONLINE");
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();

    when(clusterApiService.approveTopicRequests(
            topicName, RequestOperationType.CREATE.value, 2, "1", "1", new HashMap<>(), 101, null))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));

    mvc.perform(
            MockMvcRequestBuilders.post("/execTopicRequests")
                .with(user(user3).password(PASSWORD))
                .param("topicId", topicId3 + "")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    return topicName;
  }
}
