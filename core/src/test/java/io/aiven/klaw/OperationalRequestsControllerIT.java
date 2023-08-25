package io.aiven.klaw;

import static io.aiven.klaw.helpers.KwConstants.TENANT_CONFIG_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwPropertiesModel;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.KwClustersModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.OperationalRequestsResponseModel;
import io.aiven.klaw.service.ClusterApiService;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestPropertySource(locations = "classpath:test-application-rdbms.properties")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class,
    properties = {"spring.datasource.url=jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OperationalRequestsControllerIT {

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
  public void setup() throws Exception {
    utilMethods = new UtilMethods();
    mockMethods = new MockMethods();
    createRequiredUsers();
    addNewCluster();
    createEnv();
    updateTenantConfig();
  }

  @Test
  @Order(1)
  public void createOffsetResetRequest() throws Exception {
    ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel =
        utilMethods.getConsumerOffsetResetRequest();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(consumerOffsetResetRequestModel);
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/operationalRequest/consumerOffsetsReset/create")
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

  @Test
  @Order(2)
  public void getOffsetResetRequests() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/operationalRequest")
                    .with(user(user1).password(PASSWORD).roles("USER"))
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<OperationalRequestsResponseModel> operationalRequestList =
        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(operationalRequestList.size()).isEqualTo(1);
    assertThat(operationalRequestList.get(0).getTopicname())
        .isEqualTo(utilMethods.getConsumerOffsetResetRequest().getTopicname());
    assertThat(operationalRequestList.get(0).getConsumerGroup())
        .isEqualTo(utilMethods.getConsumerOffsetResetRequest().getConsumerGroup());
    assertThat(operationalRequestList.get(0).getOffsetResetType())
        .isEqualTo(utilMethods.getConsumerOffsetResetRequest().getOffsetResetType());
  }

  // Create user1, user2, user3 with USER role success
  public void createRequiredUsers() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel = mockMethods.getUserInfoModel(user1, role, INFRATEAM);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

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

    userInfoModel = mockMethods.getUserInfoModel(user2, role, INFRATEAM);
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

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

    userInfoModel = mockMethods.getUserInfoModel(user3, role, INFRATEAM);
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

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
  }

  public void addNewCluster() throws Exception {
    KwClustersModel kwClustersModel = mockMethods.getKafkaClusterModel("DEV_CLUSTER");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(kwClustersModel);

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
  }

  public void createEnv() throws Exception {
    EnvModel envModel = mockMethods.getEnvModel("DEV");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(envModel);

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
  }

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
  }
}
