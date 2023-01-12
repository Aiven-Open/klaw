package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.config.SslContextConfig;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.AdminClientProperties;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class ClusterApiUtilsTest {

  @Mock Environment env;
  @Mock private AdminClientProperties adminClientProperties;
  private ClusterApiUtils clusterApiUtils;

  @Mock private HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory;

  @Mock SslContextConfig sslContextConfig;

  @BeforeEach
  public void setUp() {
    clusterApiUtils = new ClusterApiUtils(env, adminClientProperties);
    ReflectionTestUtils.setField(clusterApiUtils, "sslContextConfig", sslContextConfig);
  }

  @Test
  public void getSchemaRegistryRequestDetailsSSL() {
    //    String clusterIdentification = "CLID1";
    //    String credentials = "username:password";
    String suffixUrl = "localhost:8081/subjects";
    //    String key = clusterIdentification.toLowerCase() + KAFKA_SR_CREDENTIALS_PROPERTY_SFX;
    //    when(env.getProperty(eq(key))).thenReturn(credentials);
    when(sslContextConfig.getClientHttpRequestFactory())
        .thenReturn(httpComponentsClientHttpRequestFactory);
    Pair<String, RestTemplate> templatePair =
        clusterApiUtils.getRequestDetails(suffixUrl, KafkaSupportedProtocol.SSL);

    assertThat(templatePair.getLeft()).isEqualTo(ClusterApiUtils.HTTPS_PREFIX + suffixUrl);
  }

  @Test
  public void getSchemaRegistryRequestDetailsSSLClIdNotConfigured() {
    String suffixUrl = "localhost:8081/subjects";
    when(sslContextConfig.getClientHttpRequestFactory())
        .thenReturn(httpComponentsClientHttpRequestFactory);
    Pair<String, RestTemplate> templatePair =
        clusterApiUtils.getRequestDetails(suffixUrl, KafkaSupportedProtocol.SSL);

    assertThat(templatePair.getLeft()).isEqualTo(ClusterApiUtils.HTTPS_PREFIX + suffixUrl);
    assertThat(templatePair.getRight().getRequestFactory())
        .isEqualTo(httpComponentsClientHttpRequestFactory);
  }

  @Test
  public void getSchemaRegistryRequestDetailsPlain() {
    String suffixUrl = "localhost:8081/subjects";
    Pair<String, RestTemplate> templatePair =
        clusterApiUtils.getRequestDetails(suffixUrl, KafkaSupportedProtocol.PLAINTEXT);

    assertThat(templatePair.getLeft()).isEqualTo(ClusterApiUtils.HTTP_PREFIX + suffixUrl);
  }

  @Test
  public void getConnectRequestDetailsSSL() {
    String suffixUrl = "localhost:8081/subjects";
    when(sslContextConfig.getClientHttpRequestFactory())
        .thenReturn(httpComponentsClientHttpRequestFactory);
    Pair<String, RestTemplate> templatePair =
        clusterApiUtils.getRequestDetails(suffixUrl, KafkaSupportedProtocol.SSL);

    assertThat(templatePair.getLeft()).isEqualTo(ClusterApiUtils.HTTPS_PREFIX + suffixUrl);
  }

  @Test
  public void getConnectRequestDetailsSSLClIdNotConfigured() {
    String suffixUrl = "localhost:8081/subjects";
    when(sslContextConfig.getClientHttpRequestFactory())
        .thenReturn(httpComponentsClientHttpRequestFactory);
    Pair<String, RestTemplate> templatePair =
        clusterApiUtils.getRequestDetails(suffixUrl, KafkaSupportedProtocol.SSL);

    assertThat(templatePair.getLeft()).isEqualTo(ClusterApiUtils.HTTPS_PREFIX + suffixUrl);
  }

  @Test
  public void getConnectRequestDetailsPlain() {
    String clusterIdentification = "CLID1";
    String suffixUrl = "localhost:8081/subjects";
    Pair<String, RestTemplate> templatePair =
        clusterApiUtils.getRequestDetails(suffixUrl, KafkaSupportedProtocol.PLAINTEXT);

    assertThat(templatePair.getLeft()).isEqualTo(ClusterApiUtils.HTTP_PREFIX + suffixUrl);
  }
}
