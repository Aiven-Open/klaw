package io.aiven.klaw.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.SyncTopicUpdates;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicControllerTest {

  @MockBean private TopicControllerService topicControllerService;

  @MockBean private TopicSyncControllerService topicSyncControllerService;

  private TopicController topicController;

  private TopicSyncController topicSyncController;

  private UtilMethods utilMethods;

  private MockMvc mvc, mvcSync;

  @BeforeEach
  public void setUp() throws Exception {
    topicController = new TopicController();
    topicSyncController = new TopicSyncController();
    mvc = MockMvcBuilders.standaloneSetup(topicController).dispatchOptions(true).build();
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
    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(addTopicRequest);
    ApiResponse apiResponse =
        ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).status(HttpStatus.OK).build();
    when(topicControllerService.createTopicsRequest(any())).thenReturn(apiResponse);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createTopics")
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    Map<String, String> actualResult =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(actualResult).containsEntry("result", "success");
  }

  @Test
  @Order(2)
  public void updateSyncTopics() throws Exception {
    List<SyncTopicUpdates> syncTopicUpdates = utilMethods.getSyncTopicUpdates();
    String jsonReq = new ObjectMapper().writer().writeValueAsString(syncTopicUpdates);
    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");
    when(topicSyncControllerService.updateSyncTopics(any())).thenReturn(resultMap);

    String response =
        mvcSync
            .perform(
                MockMvcRequestBuilders.post("/updateSyncTopics")
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Map<String, String> actualResult =
        new ObjectMapper().readValue(response, new TypeReference<>() {});

    assertThat(actualResult).containsEntry("result", "success");
  }

  @Test
  @Order(3)
  public void getTopicRequests() throws Exception {
    List<TopicRequestModel> topicRequests = utilMethods.getTopicRequestsModel();

    when(topicControllerService.getTopicRequests("1", "", "all")).thenReturn(topicRequests);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicRequests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<TopicRequest> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(1);
  }

  @Test
  @Order(4)
  public void getTopicTeam() throws Exception {
    String topicName = "testtopic";
    Map<String, String> teamMap = new HashMap<>();
    teamMap.put("team", "Team1");
    when(topicControllerService.getTopicTeamOnly(topicName, "LITERAL")).thenReturn(teamMap);

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
    when(topicControllerService.getCreatedTopicRequests("1", "", "created")).thenReturn(topicReqs);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getCreatedTopicRequests")
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<TopicRequestModel> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(1);
  }

  @Test
  @Order(6)
  public void deleteTopicRequests() throws Exception {
    when(topicControllerService.deleteTopicRequests(anyString())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteTopicRequests")
                    .param("topicId", "testtopic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEqualTo("success");
  }

  @Test
  @Order(7)
  public void approveTopicRequests() throws Exception {
    ApiResponse apiResponse =
        ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).status(HttpStatus.OK).build();
    when(topicControllerService.approveTopicRequests(anyString())).thenReturn(apiResponse);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execTopicRequests")
                    .param("topicId", "testtopic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse objectResponse = new ObjectMapper().readValue(response, ApiResponse.class);
    assertThat(objectResponse.getResult()).isEqualTo("success");
  }

  @Test
  @Order(8)
  public void declineTopicRequests() throws Exception {
    when(topicControllerService.declineTopicRequests(anyString(), anyString()))
        .thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execTopicRequestsDecline")
                    .param("topicId", "1001")
                    .param("reasonForDecline", "reason")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEqualTo("success");
  }

  @Test
  @Order(9)
  public void getTopics() throws Exception {
    List<List<TopicInfo>> topicList = utilMethods.getTopicInfoList();

    when(topicControllerService.getTopics(
            anyString(), anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(topicList);

    String res =
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
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<List<TopicInfo>> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(1);
  }

  @Test
  @Order(10)
  public void getTopicsOnly() throws Exception {
    List<String> topicList = Arrays.asList("testtopic1", "testtopic2");
    when(topicControllerService.getAllTopics(false)).thenReturn(topicList);
    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTopicsOnly")
                    .param("env", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<String> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(2);
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

    String res =
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
            .andReturn()
            .getResponse()
            .getContentAsString();

    HashMap response = new ObjectMapper().readValue(res, HashMap.class);
    assertThat(response).hasSize(1);
  }
}
