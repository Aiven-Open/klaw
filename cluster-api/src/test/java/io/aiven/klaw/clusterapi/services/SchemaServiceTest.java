package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
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
            eq(getSchemaVersionsUrl), eq(KafkaSupportedProtocol.PLAINTEXT)))
        .thenReturn(Pair.of(getSchemaVersionsUrl, restTemplate));
    this.mockRestServiceServer
        .expect(requestTo("/" + getSchemaVersionsUrl))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(Lists.list(1)), MediaType.APPLICATION_JSON));

    // getSchemaCompatibility
    String getSchemaCompatibilityUrl = "env/config/topic-value";
    when(getAdminClient.getRequestDetails(
            eq(getSchemaCompatibilityUrl), eq(KafkaSupportedProtocol.PLAINTEXT)))
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
    when(getAdminClient.getRequestDetails(eq(getSchemaUrl), eq(KafkaSupportedProtocol.PLAINTEXT)))
        .thenReturn(Pair.of(getSchemaUrl, restTemplate));

    when(getAdminClient.getRequestDetails(eq(getSchemaUrl), eq(KafkaSupportedProtocol.PLAINTEXT)))
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

  @Test
  public void deleteSchema() {
    ClusterTopicRequest clusterTopicRequest = deleteTopicRequest("testtopic");

    String deleteSchemaUrl = "schemaservers/subjects/testtopic-value";
    when(getAdminClient.getRequestDetails(eq(deleteSchemaUrl), eq(KafkaSupportedProtocol.SSL)))
        .thenReturn(Pair.of(deleteSchemaUrl, restTemplate));

    this.mockRestServiceServer.expect(requestTo("/" + deleteSchemaUrl)).andRespond(withSuccess());

    ApiResponse apiResponse = schemaService.deleteSchema(clusterTopicRequest);
    assertThat(apiResponse.getResult())
        .isEqualTo("Schema deletion " + ApiResultStatus.SUCCESS.value);
  }

  private static ClusterTopicRequest deleteTopicRequest(String topicName) {
    return ClusterTopicRequest.builder()
        .clusterName("DEV2")
        .topicName(topicName)
        .env("bootStrapServersSsl")
        .protocol(KafkaSupportedProtocol.SSL)
        .partitions(1)
        .replicationFactor(Short.parseShort("1"))
        .aclsNativeType(AclsNativeType.NATIVE)
        .deleteAssociatedSchema(true)
        .schemaClusterIdentification("DEV3")
        .schemaEnv("schemaservers")
        .schemaEnvProtocol(KafkaSupportedProtocol.SSL)
        .build();
  }
}
