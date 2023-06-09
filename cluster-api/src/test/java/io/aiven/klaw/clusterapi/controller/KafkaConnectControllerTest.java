package io.aiven.klaw.clusterapi.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.connect.ConnectorsStatus;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.KafkaConnectService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
public class KafkaConnectControllerTest {
  private MockMvc mvc;

  @MockBean KafkaConnectService kafkaConnectService;

  UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    utilMethods = new UtilMethods();
    KafkaConnectController connectController = new KafkaConnectController();
    mvc = MockMvcBuilders.standaloneSetup(connectController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(connectController, "kafkaConnectService", kafkaConnectService);
  }

  @Test
  public void getAllConnectorsTest() throws Exception {
    String getUrl = "/topics/getAllConnectors/localhost/" + KafkaSupportedProtocol.SSL + "/CLID1";
    ConnectorsStatus connectors = utilMethods.getConnectorsStatus();
    when(kafkaConnectService.getConnectors(anyString(), any(), anyString())).thenReturn(connectors);

    mvc.perform(get(getUrl))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.connectorStateList", hasSize(2)))
        .andExpect(content().string(containsString("conn1")))
        .andExpect(content().string(containsString("conn2")));
  }

  @Test
  public void getAllConnectorsInvalidProtocolTest() throws Exception {
    String getUrl = "/topics/getAllConnectors/localhost/" + "INVALIDPROTOCOL" + "/CLID1";
    ConnectorsStatus connectors = utilMethods.getConnectorsStatus();
    when(kafkaConnectService.getConnectors(anyString(), any(), anyString())).thenReturn(connectors);

    mvc.perform(get(getUrl)).andExpect(status().is4xxClientError());
  }

  @Test
  public void getAllConnectorsClusterCallFailureTest() throws Exception {
    String getUrl = "/topics/getAllConnectors/localhost/" + KafkaSupportedProtocol.SSL + "/CLID1";
    ConnectorsStatus connectors = new ConnectorsStatus();
    connectors.setConnectorStateList(new ArrayList<>());
    when(kafkaConnectService.getConnectors(anyString(), any(), anyString())).thenReturn(connectors);
    mvc.perform(get(getUrl))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.connectorStateList", hasSize(0)));
  }

  @Test
  public void getConnectorDetailsTest() throws Exception {
    String getUrl =
        "/topics/getConnectorDetails/connectorName/kafkaConnectHost/ "
            + KafkaSupportedProtocol.SSL
            + "/CLID1";
    Map<String, Object> connDetails = new HashMap<>();
    connDetails.put("tasks.max", "4");
    when(kafkaConnectService.getConnectorDetails(anyString(), anyString(), any(), anyString()))
        .thenReturn(connDetails);

    mvc.perform(get(getUrl))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasKey("tasks.max")));
  }

  @Test
  public void getConnectorDetailsInvalidProtocolTest() throws Exception {
    String getUrl =
        "/topics/getConnectorDetails/connectorName/kafkaConnectHost/ "
            + "INVALIDPROTOCOL"
            + "/CLID1";
    Map<String, Object> connDetails = new HashMap<>();
    connDetails.put("tasks.max", "4");
    when(kafkaConnectService.getConnectorDetails(anyString(), anyString(), any(), anyString()))
        .thenReturn(connDetails);

    mvc.perform(get(getUrl)).andExpect(status().is4xxClientError());
  }

  @Test
  public void getConnectorDetailsClusterCallFailureTest() throws Exception {
    String getUrl =
        "/topics/getConnectorDetails/connectorName/kafkaConnectHost/ "
            + KafkaSupportedProtocol.SSL
            + "/CLID1";

    mvc.perform(get(getUrl)).andExpect(status().isOk()).andExpect(jsonPath("$", aMapWithSize(0)));
    ;
  }

  @Test
  public void postConnectorTest() throws Exception {
    String postUrl = "/topics/postConnector";
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ClusterConnectorRequest clusterConnectorRequest = getClusterConnectorRequest();
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterConnectorRequest);
    when(kafkaConnectService.postNewConnector(any())).thenReturn(apiResponse);

    mvc.perform(post(postUrl).content(jsonReq).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void postConnectorClusterFailureTest() throws Exception {
    String postUrl = "/topics/postConnector";
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.FAILURE.value).build();
    ClusterConnectorRequest clusterConnectorRequest = getClusterConnectorRequest();
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterConnectorRequest);
    when(kafkaConnectService.postNewConnector(any())).thenReturn(apiResponse);

    mvc.perform(post(postUrl).content(jsonReq).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(ApiResultStatus.FAILURE.value)));
  }

  @Test
  public void updateConnector() throws Exception {
    String postUrl = "/topics/updateConnector";
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ClusterConnectorRequest clusterConnectorRequest = getClusterConnectorRequest();
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterConnectorRequest);
    when(kafkaConnectService.updateConnector(any())).thenReturn(apiResponse);

    mvc.perform(post(postUrl).content(jsonReq).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void deleteConnector() throws Exception {
    String postUrl = "/topics/deleteConnector";
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ClusterConnectorRequest clusterConnectorRequest = getClusterConnectorRequest();
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterConnectorRequest);
    when(kafkaConnectService.deleteConnector(any())).thenReturn(apiResponse);

    mvc.perform(post(postUrl).content(jsonReq).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  private static ClusterConnectorRequest getClusterConnectorRequest() {
    return ClusterConnectorRequest.builder()
        .clusterIdentification("CLID1")
        .connectorConfig("{connectorname:test}")
        .connectorName("test")
        .env("localhost:843")
        .protocol(KafkaSupportedProtocol.SSL)
        .build();
  }
}
