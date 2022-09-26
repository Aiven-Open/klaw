package io.aiven.klaw.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.AclRequestsModel;
import io.aiven.klaw.model.SyncAclUpdates;
import io.aiven.klaw.model.TopicOverview;
import io.aiven.klaw.service.AclControllerService;
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

  @MockBean private AclControllerService aclControllerService;

  private UtilMethods utilMethods;

  private MockMvc mvc;

  private AclController aclController;

  private static final String topicName = "testtopic";
  private static final int topicId = 1001;

  @BeforeEach
  public void setup() {
    aclController = new AclController();
    utilMethods = new UtilMethods();
    mvc = MockMvcBuilders.standaloneSetup(aclController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(aclController, "aclControllerService", aclControllerService);
  }

  @Test
  @Order(1)
  public void createAcl() throws Exception {
    AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName + topicId);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);
    when(aclControllerService.createAcl(any())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/createAcl")
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEqualTo("success");
  }

  @Test
  public void updateSyncAcls() throws Exception {
    List<SyncAclUpdates> syncUpdates = utilMethods.getSyncAclsUpdates();

    String jsonReq = new ObjectMapper().writer().writeValueAsString(syncUpdates);
    Map<String, String> result = new HashMap<>();
    result.put("result", "success");
    when(aclControllerService.updateSyncAcls(any())).thenReturn(result);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/updateSyncAcls")
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Map<String, String> actualResult =
        new ObjectMapper().readValue(response, new TypeReference<Map<String, String>>() {});

    assertThat(actualResult).containsEntry("result", "success");
  }

  @Test
  public void getAclRequests() throws Exception {

    List<AclRequestsModel> aclRequests = utilMethods.getAclRequestsModel();

    when(aclControllerService.getAclRequests("1", "", "all")).thenReturn(aclRequests);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getAclRequests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("pageNo", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<AclRequestsModel> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(1);
  }

  @Test
  public void getCreatedAclRequests() throws Exception {

    List<AclRequestsModel> aclRequests = utilMethods.getAclRequestsList();

    when(aclControllerService.getCreatedAclRequests("1", "", "created")).thenReturn(aclRequests);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getCreatedAclRequests")
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<List<AclRequestsModel>> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(1);
  }

  @Test
  public void deleteAclRequests() throws Exception {
    when(aclControllerService.deleteAclRequests(anyString())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteAclRequests")
                    .param("req_no", "fsda32FSDw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEqualTo("success");
  }

  @Test
  public void approveAclRequests() throws Exception {
    when(aclControllerService.approveAclRequests(anyString())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execAclRequest")
                    .param("req_no", "reqno")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEqualTo("success");
  }

  @Test
  public void declineAclRequests() throws Exception {
    when(aclControllerService.declineAclRequests(anyString(), anyString())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execAclRequestDecline")
                    .param("req_no", "reqno")
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
  public void getAcls1() throws Exception {
    TopicOverview topicOverview = utilMethods.getTopicOverview();

    when(aclControllerService.getAcls("testtopic")).thenReturn(topicOverview);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getAcls")
                    .param("topicnamesearch", "testtopic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TopicOverview response = new ObjectMapper().readValue(res, TopicOverview.class);
    assertThat(response.getAclInfoList()).hasSize(1);
  }

  @Test
  public void getAcls2() throws Exception {
    TopicOverview topicOverview = utilMethods.getTopicOverview();

    when(aclControllerService.getAcls(null)).thenReturn(topicOverview);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getAcls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
  }

  @Test
  public void getSyncAcls() throws Exception {
    List<AclInfo> aclInfo = utilMethods.getAclInfoList();

    when(aclControllerService.getSyncAcls(anyString(), anyString(), anyString(), any(), any()))
        .thenReturn(aclInfo);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getSyncAcls")
                    .param("env", "DEV")
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<AclInfo> response = new ObjectMapper().readValue(res, List.class);
    assertThat(response).hasSize(1);
  }
}
