package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

@RestClientTest(SchemaService.class)
class SchemaServiceTest {

  @Autowired SchemaService schemaService;

  RestTemplate restTemplate;
  @Autowired ObjectMapper objectMapper;
  private MockRestServiceServer mockRestServiceServer;
  @MockBean private ClusterApiUtils getAdminClient;

  @BeforeEach
  public void setUp() {
    restTemplate = new RestTemplate();
    schemaService = new SchemaService(getAdminClient);
    mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
  }

  // TODO need to add proper return value
  @Test
  public void getSchema_returnMap() throws JsonProcessingException {
    // getSchemaVersions
    String getSchemaVersionsUrl = "env/subjects/topic-value/versions";
    when(getAdminClient.getRequestDetails(
            eq(getSchemaVersionsUrl),
            eq(KafkaSupportedProtocol.PLAINTEXT),
            eq(KafkaClustersType.SCHEMA_REGISTRY),
            anyString()))
        .thenReturn(Pair.of(getSchemaVersionsUrl, restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/" + getSchemaVersionsUrl))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(Lists.list(1)), MediaType.APPLICATION_JSON));

    // getSchemaCompatibility
    String getSchemaCompatibilityUrl = "env/config/topic-value";
    when(getAdminClient.getRequestDetails(
            eq(getSchemaCompatibilityUrl),
            eq(KafkaSupportedProtocol.PLAINTEXT),
            eq(KafkaClustersType.SCHEMA_REGISTRY),
            anyString()))
        .thenReturn(Pair.of(getSchemaCompatibilityUrl, restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/" + getSchemaCompatibilityUrl))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(
                    Collections.singletonMap("compatibilityLevel", "great")),
                MediaType.APPLICATION_JSON));

    // getSchema
    String getSchemaUrl = "env/subjects/topic-value/versions/1";
    when(getAdminClient.getRequestDetails(
            eq(getSchemaUrl),
            eq(KafkaSupportedProtocol.PLAINTEXT),
            eq(KafkaClustersType.SCHEMA_REGISTRY),
            anyString()))
        .thenReturn(Pair.of(getSchemaUrl, restTemplate));

    when(getAdminClient.getRequestDetails(
            eq(getSchemaUrl), eq(KafkaSupportedProtocol.PLAINTEXT), any(), anyString()))
        .thenReturn(Pair.of(getSchemaUrl, restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/" + getSchemaUrl))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(Collections.singletonMap("foo", "bar")),
                MediaType.APPLICATION_JSON));

    assertThat(schemaService.getSchema("env", KafkaSupportedProtocol.PLAINTEXT, "CLID1", "topic"))
        .isNotEmpty();
  }
}
