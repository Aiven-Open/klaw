package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncAclUpdates;
import io.aiven.klaw.model.enums.AclGroupBy;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.TopicOverview;
import io.aiven.klaw.service.AclControllerService;
import io.aiven.klaw.service.AclSyncControllerService;
import io.aiven.klaw.service.TopicOverviewService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclControllerTest {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String topicName = "testtopic";
  private static final int topicId = 1001;
  @MockBean private AclControllerService aclControllerService;
  @MockBean private AclSyncControllerService aclSyncControllerService;

  @MockBean private TopicOverviewService topicOverviewService;
  private UtilMethods utilMethods;
  private MockMvc mvcAcls;
  private AclController aclController;
  private MockMvc mvcAclsSync;
  private AclSyncController aclSyncController;

  @BeforeEach
  public void setup() {
    aclController = new AclController();
    aclSyncController = new AclSyncController();
    utilMethods = new UtilMethods();
    mvcAcls = MockMvcBuilders.standaloneSetup(aclController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(aclController, "aclControllerService", aclControllerService);
    ReflectionTestUtils.setField(aclController, "topicOverviewService", topicOverviewService);
    mvcAclsSync = MockMvcBuilders.standaloneSetup(aclSyncController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(
        aclSyncController, "aclSyncControllerService", aclSyncControllerService);
  }

  @Test
  @Order(1)
  public void createAcl() throws Exception {
    AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName + topicId);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addAclRequest);
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(aclControllerService.createAcl(any())).thenReturn(apiResponse);

    mvcAcls
        .perform(
            MockMvcRequestBuilders.post("/createAcl")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void updateSyncAcls() throws Exception {
    List<SyncAclUpdates> syncUpdates = utilMethods.getSyncAclsUpdates();

    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(syncUpdates);

    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(aclSyncControllerService.updateSyncAcls(any())).thenReturn(apiResponse);

    mvcAclsSync
        .perform(
            MockMvcRequestBuilders.post("/updateSyncAcls")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void getAclRequests() throws Exception {

    List<AclRequestsResponseModel> aclRequests = utilMethods.getAclRequestsModel();

    when(aclControllerService.getAclRequests(
            "1",
            "",
            "all",
            null,
            null,
            null,
            null,
            null,
            io.aiven.klaw.model.enums.Order.DESC_REQUESTED_TIME,
            false))
        .thenReturn(aclRequests);

    mvcAcls
        .perform(
            MockMvcRequestBuilders.get("/getAclRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo", "1")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void getCreatedAclRequests() throws Exception {

    List<AclRequestsResponseModel> aclRequests = utilMethods.getAclRequestsList();

    when(aclControllerService.getAclRequestsForApprover(
            "1",
            "",
            "created",
            null,
            null,
            RequestOperationType.CREATE,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME))
        .thenReturn(aclRequests);

    mvcAcls
        .perform(
            MockMvcRequestBuilders.get("/getAclRequestsForApprover")
                .param("pageNo", "1")
                .param("operationType", RequestOperationType.CREATE.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void deleteAclRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(aclControllerService.deleteAclRequests(anyString())).thenReturn(apiResponse);
    mvcAcls
        .perform(
            MockMvcRequestBuilders.post("/deleteAclRequests")
                .param("req_no", "fsda32FSDw")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void approveAclRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(aclControllerService.approveAclRequests(anyString())).thenReturn(apiResponse);

    mvcAcls
        .perform(
            MockMvcRequestBuilders.post("/execAclRequest")
                .param("req_no", "reqno")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void declineAclRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(aclControllerService.declineAclRequests(anyString(), anyString())).thenReturn(apiResponse);
    mvcAcls
        .perform(
            MockMvcRequestBuilders.post("/execAclRequestDecline")
                .param("req_no", "reqno")
                .param("reasonForDecline", "reason")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void getAcls1() throws Exception {
    TopicOverview topicOverview = utilMethods.getTopicOverview();

    when(topicOverviewService.getTopicOverview("testtopic", AclGroupBy.TEAM))
        .thenReturn(topicOverview);

    mvcAcls
        .perform(
            MockMvcRequestBuilders.get("/getAcls")
                .param("topicnamesearch", "testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topicInfoList[*]", hasSize(1)))
        .andExpect(jsonPath("$.aclInfoList[*]", hasSize(1)));
  }

  @Test
  public void getAcls2() throws Exception {
    TopicOverview topicOverview = utilMethods.getTopicOverview();

    when(topicOverviewService.getTopicOverview(null, AclGroupBy.IP)).thenReturn(topicOverview);

    // TODO Consider returning an error response object (https://www.rfc-editor.org/rfc/rfc7807)
    // Just checking response code seems to be sufficient as the contentAsString() returns an empty
    // String.
    mvcAcls
        .perform(
            MockMvcRequestBuilders.get("/getAcls")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getSyncAcls() throws Exception {
    List<AclInfo> aclInfo = utilMethods.getAclInfoList();

    when(aclSyncControllerService.getSyncAcls(anyString(), anyString(), anyString(), any(), any()))
        .thenReturn(aclInfo);

    mvcAclsSync
        .perform(
            MockMvcRequestBuilders.get("/getSyncAcls")
                .param("env", "DEV")
                .param("pageNo", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void getAivenServiceAccount() throws Exception {
    Map<String, String> serviceAccountInfoMap = new HashMap<>();
    serviceAccountInfoMap.put("password", "password");
    serviceAccountInfoMap.put("username", "username");

    ApiResponse apiResponse =
        ApiResponse.builder()
            .message(ApiResultStatus.SUCCESS.value)
            .data(serviceAccountInfoMap)
            .build();
    when(aclControllerService.getAivenServiceAccountDetails(
            anyString(), anyString(), anyString(), anyString()))
        .thenReturn(apiResponse);

    mvcAcls
        .perform(
            MockMvcRequestBuilders.get("/getAivenServiceAccount")
                .param("env", "DEV")
                .param("topicName", "testtopic")
                .param("userName", "kwuser")
                .param("aclReqNo", "101")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", aMapWithSize(2)));
  }
}
