package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Collections;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(KafkaConnectService.class)
class KafkaConnectServiceTest {

  @Autowired KafkaConnectService kafkaConnectService;

  RestTemplate restTemplate;
  @Autowired ObjectMapper objectMapper;
  private MockRestServiceServer mockRestServiceServer;
  @MockBean private ClusterApiUtils getAdminClient;

  @BeforeEach
  public void setUp() {
    restTemplate = new RestTemplate();
    kafkaConnectService = new KafkaConnectService(getAdminClient);
    mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
  }

  // TODO need to add proper return value
  @Test
  public void getConnectors_returnList() throws JsonProcessingException {
    when(getAdminClient.getRequestDetails(any(), eq(KafkaSupportedProtocol.PLAINTEXT), any()))
        .thenReturn(Pair.of("/env/connectors", restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/env/connectors"))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(Lists.list("conn1", "conn2")),
                MediaType.APPLICATION_JSON));

    assertThat(kafkaConnectService.getConnectors("env", KafkaSupportedProtocol.PLAINTEXT))
        .isNotEmpty();
  }

  // TODO need to add proper return value
  @Test
  public void getConnectorDetails_returnMap() throws JsonProcessingException {
    when(getAdminClient.getRequestDetails(
            any(), eq(KafkaSupportedProtocol.PLAINTEXT), eq(KafkaClustersType.KAFKA_CONNECT)))
        .thenReturn(Pair.of("/env/connectors/conn1", restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/env/connectors/conn1"))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(Collections.singletonMap("conn1", "conn2")),
                MediaType.APPLICATION_JSON));

    assertThat(
            kafkaConnectService.getConnectorDetails(
                "conn1", "env", KafkaSupportedProtocol.PLAINTEXT))
        .isNotEmpty();
  }
}
