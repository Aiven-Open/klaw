package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withRawStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.error.RestErrorResponse;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Collections;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(KafkaConnectService.class)
class KafkaConnectServiceTest {

  public static final String THIS_IS_A_MISCONFIGURED_CONNECTOR =
      "This is a misconfigured connector";
  @Autowired KafkaConnectService kafkaConnectService;

  RestTemplate restTemplate;
  @Autowired ObjectMapper objectMapper;
  private MockRestServiceServer mockRestServiceServer;
  @MockBean private ClusterApiUtils getAdminClient;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    utilMethods = new UtilMethods();
    restTemplate = new RestTemplate();
    kafkaConnectService = new KafkaConnectService(getAdminClient);
    mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
  }

  // TODO need to add proper return value
  @Test
  public void getConnectors_returnList() throws JsonProcessingException {
    when(getAdminClient.getRequestDetails(any(), eq(KafkaSupportedProtocol.PLAINTEXT)))
        .thenReturn(Pair.of("/env/connectors", restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/env/connectors"))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(utilMethods.getConnectorsListMap()),
                MediaType.APPLICATION_JSON));

    assertThat(
            kafkaConnectService
                .getConnectors("env", KafkaSupportedProtocol.PLAINTEXT, "CLID1", true)
                .getConnectorStateList())
        .isNotEmpty();
  }

  // TODO need to add proper return value
  @Test
  public void getConnectorDetails_returnMap() throws JsonProcessingException {
    when(getAdminClient.getRequestDetails(any(), eq(KafkaSupportedProtocol.PLAINTEXT)))
        .thenReturn(Pair.of("/env/connectors/conn1", restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(Collections.singletonMap("conn1", "conn2")),
                MediaType.APPLICATION_JSON));

    assertThat(
            kafkaConnectService.getConnectorDetails(
                "conn1", "env", KafkaSupportedProtocol.PLAINTEXT, "CLID1"))
        .isNotEmpty();
  }

  @Test
  public void createConnector_bad_request() throws Exception {
    ClusterConnectorRequest connectorRequest = stubCreateOrDeleteConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(
            withBadRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(getRestErrorResponse(THIS_IS_A_MISCONFIGURED_CONNECTOR)));
    ApiResponse connectorResponse = kafkaConnectService.postNewConnector(connectorRequest);

    assertThat(connectorResponse.isSuccess()).isFalse();
    assertThat(connectorResponse.getMessage()).isEqualTo(THIS_IS_A_MISCONFIGURED_CONNECTOR);
  }

  private String getRestErrorResponse(String resp) throws JsonProcessingException {
    RestErrorResponse restErrorResponse = new RestErrorResponse();
    restErrorResponse.setErrorCode(400);
    restErrorResponse.setMessage(resp);
    return objectMapper.writeValueAsString(restErrorResponse);
  }

  @Test
  public void createConnector_success() throws Exception {
    ClusterConnectorRequest connectorRequest = stubCreateOrDeleteConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(withRawStatus(201).contentType(MediaType.APPLICATION_JSON));
    ApiResponse connectorResponse = kafkaConnectService.postNewConnector(connectorRequest);
    assertThat(connectorResponse.isSuccess()).isTrue();
    assertThat(connectorResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createConnector_fail() throws Exception {
    ClusterConnectorRequest connectorRequest = stubCreateOrDeleteConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(withRawStatus(207).contentType(MediaType.APPLICATION_JSON));
    ApiResponse connectorResponse = kafkaConnectService.postNewConnector(connectorRequest);
    assertThat(connectorResponse.isSuccess()).isFalse();
    assertThat(connectorResponse.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);
  }

  @Test
  public void updateConnector_success() {
    ClusterConnectorRequest connectorRequest = stubUpdateConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1/config"))
        .andRespond(withRawStatus(201).contentType(MediaType.APPLICATION_JSON));
    ApiResponse connectorResponse = kafkaConnectService.updateConnector(connectorRequest);
    assertThat(connectorResponse.isSuccess()).isTrue();
    assertThat(connectorResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateConnector_badRequest() throws JsonProcessingException {
    ClusterConnectorRequest connectorRequest = stubUpdateConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1/config"))
        .andRespond(
            withRawStatus(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(getRestErrorResponse(THIS_IS_A_MISCONFIGURED_CONNECTOR)));
    ApiResponse connectorResponse = kafkaConnectService.updateConnector(connectorRequest);

    assertThat(connectorResponse.isSuccess()).isFalse();
    assertThat(connectorResponse.getMessage()).isEqualTo(THIS_IS_A_MISCONFIGURED_CONNECTOR);
  }

  @Test
  public void deleteConnector_badRequest() throws JsonProcessingException {
    ClusterConnectorRequest connectorRequest = stubCreateOrDeleteConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(
            withRawStatus(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(getRestErrorResponse(THIS_IS_A_MISCONFIGURED_CONNECTOR)));
    ApiResponse connectorResponse = kafkaConnectService.deleteConnector(connectorRequest);

    assertThat(connectorResponse.isSuccess()).isFalse();
    assertThat(connectorResponse.getMessage()).isEqualTo(THIS_IS_A_MISCONFIGURED_CONNECTOR);
  }

  @Test
  public void deleteConnector_badRequest_undetermined_response() throws JsonProcessingException {
    ClusterConnectorRequest connectorRequest = stubCreateOrDeleteConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(
            withRawStatus(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(THIS_IS_A_MISCONFIGURED_CONNECTOR));
    ApiResponse connectorResponse = kafkaConnectService.deleteConnector(connectorRequest);

    assertThat(connectorResponse.isSuccess()).isFalse();
    assertThat(connectorResponse.getMessage()).isEqualTo("Unable To Delete Connector on Cluster.");
  }

  @Test
  public void deleteConnector_success() {
    ClusterConnectorRequest connectorRequest = stubCreateOrDeleteConnector();

    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(withRawStatus(201).contentType(MediaType.APPLICATION_JSON));
    ApiResponse connectorResponse = kafkaConnectService.deleteConnector(connectorRequest);
    assertThat(connectorResponse.isSuccess()).isTrue();
    assertThat(connectorResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  private ClusterConnectorRequest stubCreateOrDeleteConnector() {
    when(getAdminClient.getRequestDetails(any(), eq(KafkaSupportedProtocol.PLAINTEXT)))
        .thenReturn(Pair.of("/env/connectors/conn1", restTemplate));
    when(getAdminClient.createHeaders(eq("1"), eq(KafkaClustersType.KAFKA_CONNECT)))
        .thenReturn(new HttpHeaders());
    ClusterConnectorRequest connectorRequest =
        ClusterConnectorRequest.builder()
            .connectorName("conn1")
            .clusterIdentification("1")
            .env("env")
            .protocol(KafkaSupportedProtocol.PLAINTEXT)
            .build();
    return connectorRequest;
  }

  private ClusterConnectorRequest stubUpdateConnector() {
    when(getAdminClient.getRequestDetails(any(), eq(KafkaSupportedProtocol.PLAINTEXT)))
        .thenReturn(Pair.of("/env/connectors/conn1/config", restTemplate));
    when(getAdminClient.createHeaders(eq("1"), eq(KafkaClustersType.KAFKA_CONNECT)))
        .thenReturn(new HttpHeaders());
    ClusterConnectorRequest connectorRequest =
        ClusterConnectorRequest.builder()
            .connectorName("conn1")
            .clusterIdentification("1")
            .env("env")
            .protocol(KafkaSupportedProtocol.PLAINTEXT)
            .build();
    return connectorRequest;
  }
}
