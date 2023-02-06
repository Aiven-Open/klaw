package io.aiven.klaw.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncTopicUpdates;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.service.TopicControllerService;
import io.aiven.klaw.service.TopicSyncControllerService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicControllerTest {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  @MockBean private TopicControllerService topicControllerService;

  @MockBean private TopicSyncControllerService topicSyncControllerService;

  private UtilMethods utilMethods;

  @Mock private Validator validator;

  private MockMvc mvc, mvcSync;

  @BeforeEach
  public void setUp() throws Exception {
    TopicController topicController = new TopicController();
    TopicSyncController topicSyncController = new TopicSyncController();
    mvc =
        MockMvcBuilders.standaloneSetup(topicController)
            .setValidator(validator)
            .dispatchOptions(true)
            .build();
    utilMethods = new UtilMethods();
    mvcSync = MockMvcBuilders.standaloneSetup(topicSyncController).dispatchOptions(true).build();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(topicController, "topicControllerService", topicControllerService);
    ReflectionTestUtils.setField(
        topicSyncController, "topicSyncControllerService", topicSyncControllerService);
  }

  @Test
  @Order(1)
  public void createTopics() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    ApiResponse apiResponse =
        ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).status(HttpStatus.OK).build();
    when(topicControllerService.createTopicsCreateRequest(any())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/createTopics")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(2)
  public void updateSyncTopics() throws Exception {
    List<SyncTopicUpdates> syncTopicUpdates = utilMethods.getSyncTopicUpdates();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(syncTopicUpdates);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(topicSyncControllerService.updateSyncTopics(any())).thenReturn(apiResponse);

    mvcSync
        .perform(
            MockMvcRequestBuilders.post("/updateSyncTopics")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(3)
  public void getTopicRequests() throws Exception {
    List<TopicRequestModel> topicRequests = utilMethods.getTopicRequestsModel();

    when(topicControllerService.getTopicRequests("1", "", "all")).thenReturn(topicRequests);

    mvc.perform(
            MockMvcRequestBuilders.get("/getTopicRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo", "1")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(4)
  public void getTopicTeam() throws Exception {
    String topicName = "testtopic";
    Map<String, String> teamMap = new HashMap<>();
    teamMap.put("team", "Team1");
    when(topicControllerService.getTopicTeamOnly(topicName, AclPatternType.LITERAL))
        .thenReturn(teamMap);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicTeam")
                    .param("topicName", topicName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    Map<String, String> resp =
        new ObjectMapper().readValue(res, new TypeReference<Map<String, String>>() {});
    assertThat(resp).containsEntry("team", "Team1");
  }

  @Test
  @Order(5)
  public void getCreatedTopicRequests() throws Exception {
    List<TopicRequestModel> topicReqs = utilMethods.getTopicRequestsList();
    when(topicControllerService.getTopicRequestsForApprover("1", "", "created"))
        .thenReturn(topicReqs);

    mvc.perform(
            MockMvcRequestBuilders.get("/getTopicRequestsForApprover")
                .param("pageNo", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(6)
  public void deleteTopicRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(topicControllerService.deleteTopicRequests(anyString())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/deleteTopicRequests")
                .param("topicId", "testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(7)
  public void approveTopicRequests() throws Exception {
    ApiResponse apiResponse =
        ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).status(HttpStatus.OK).build();
    when(topicControllerService.approveTopicRequests(anyString())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/execTopicRequests")
                .param("topicId", "testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(8)
  public void declineTopicRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(topicControllerService.declineTopicRequests(anyString(), anyString()))
        .thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/execTopicRequestsDecline")
                .param("topicId", "1001")
                .param("reasonForDecline", "reason")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(9)
  public void getTopics() throws Exception {
    List<List<TopicInfo>> topicList = utilMethods.getTopicInfoList();

    when(topicControllerService.getTopics(
            anyString(), anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(topicList);

    mvc.perform(
            MockMvcRequestBuilders.get("/getTopics")
                .param("env", "1")
                .param("pageNo", "1")
                .param("topicnamesearch", "testtopic")
                .param("teamName", "Team1")
                .param("topicType", "")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(10)
  public void getTopicsOnly() throws Exception {
    List<String> topicList = Arrays.asList("testtopic1", "testtopic2");
    when(topicControllerService.getAllTopics(false, "ALL")).thenReturn(topicList);

    mvc.perform(
            MockMvcRequestBuilders.get("/getTopicsOnly")
                .param("env", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @Order(11)
  public void getSyncTopics() throws Exception {
    Map<String, Object> hashMap = new HashMap<>();
    hashMap.put("", "");
    List<TopicRequestModel> topicRequests = utilMethods.getTopicRequestsModel();

    when(topicSyncControllerService.getSyncTopics(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(hashMap);

    mvcSync
        .perform(
            MockMvcRequestBuilders.get("/getSyncTopics")
                .param("env", "1")
                .param("pageNo", "1")
                .param("topicnamesearch", "testtopic")
                .param("showAllTopics", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(1)));
  }

  @Test
  @Order(12)
  public void updateTopic() throws Exception {
    TopicRequestModel addTopicRequest = utilMethods.getTopicUpdateRequestModel(1001);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addTopicRequest);
    ApiResponse apiResponse =
        ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).status(HttpStatus.OK).build();
    when(topicControllerService.createTopicsUpdateRequest(any())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/updateTopics")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }
}
