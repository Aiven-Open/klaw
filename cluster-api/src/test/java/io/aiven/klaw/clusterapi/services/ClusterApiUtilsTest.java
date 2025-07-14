package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.config.SslContextConfig;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.AdminClientProperties;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Properties;
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

  @Test
  public void testGetHash() {
    String input = "testEnvHost";
    String hash = clusterApiUtils.getHash(input);
    // The hash should be deterministic for the same input
    assertThat(hash).isEqualTo(clusterApiUtils.getHash(input));
    // Should not be null or empty
    assertThat(hash).isNotNull();
    assertThat(hash).isNotEmpty();
  }

  @Test
  public void testGetAdminClientProperties() {
    assertThat(clusterApiUtils.getAdminClientProperties()).isEqualTo(adminClientProperties);
  }

  @Test
  public void testGetPlainProperties() {
    String env = "localhost:9092";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    var props = clusterApiUtils.getPlainProperties(env);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(env);
    assertThat(props.getProperty("retries")).isEqualTo("3");
    assertThat(props.getProperty("request.timeout.ms")).isEqualTo("1000");
    assertThat(props.getProperty("retry.backoff.ms")).isEqualTo("100");
  }

  @Test
  public void testGetSslProperties() {
    String env = "localhost:9093";
    String clusterId = "CLID1";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    var props = clusterApiUtils.getSslProperties(env, clusterId);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(env);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SSL");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientssl");
  }

  @Test
  public void testGetSaslPlainProperties() {
    String envStr = "localhost:9094";
    String clusterId = "CLID2";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    when(env.getProperty("kafkasasl.saslmechanism.plain")).thenReturn("PLAIN");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.jaasconfig.plain"))
        .thenReturn("jaasConfig");
    var props = clusterApiUtils.getSaslPlainProperties(envStr, clusterId);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(envStr);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SASL_PLAINTEXT");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientsaslplain");
    assertThat(props.getProperty("sasl.mechanism")).isEqualTo("PLAIN");
    assertThat(props.getProperty("sasl.jaas.config")).isEqualTo("jaasConfig");
  }

  @Test
  public void testGetSaslSsl_PlainMechanismProperties() {
    String envStr = "localhost:9095";
    String clusterId = "CLID3";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    when(env.getProperty("kafkasasl.saslmechanism.plain")).thenReturn("PLAIN");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.jaasconfig.plain"))
        .thenReturn("jaasConfig");
    var props = clusterApiUtils.getSaslSsl_PlainMechanismProperties(envStr, clusterId);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(envStr);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SASL_SSL");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientsaslsslplain");
    assertThat(props.getProperty("sasl.mechanism")).isEqualTo("PLAIN");
    assertThat(props.getProperty("sasl.jaas.config")).isEqualTo("jaasConfig");
  }

  @Test
  public void testGetSaslSsl_ScramMechanismProperties256() {
    String envStr = "localhost:9096";
    String clusterId = "CLID4";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    when(env.getProperty("kafkasasl.saslmechanism.scram.256")).thenReturn("SCRAM-SHA-256");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.jaasconfig.scram"))
        .thenReturn("jaasConfigScram");
    var props =
        clusterApiUtils.getSaslSsl_ScramMechanismProperties(
            envStr, clusterId, ClusterApiUtils.SHA_256);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(envStr);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SASL_SSL");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientsaslsslscram");
    assertThat(props.getProperty("sasl.mechanism")).isEqualTo("SCRAM-SHA-256");
    assertThat(props.getProperty("sasl.jaas.config")).isEqualTo("jaasConfigScram");
  }

  @Test
  public void testGetSaslSsl_ScramMechanismProperties512() {
    String envStr = "localhost:9097";
    String clusterId = "CLID5";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    when(env.getProperty("kafkasasl.saslmechanism.scram.512")).thenReturn("SCRAM-SHA-512");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.jaasconfig.scram"))
        .thenReturn("jaasConfigScram512");
    var props =
        clusterApiUtils.getSaslSsl_ScramMechanismProperties(
            envStr, clusterId, ClusterApiUtils.SHA_512);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(envStr);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SASL_SSL");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientsaslsslscram");
    assertThat(props.getProperty("sasl.mechanism")).isEqualTo("SCRAM-SHA-512");
    assertThat(props.getProperty("sasl.jaas.config")).isEqualTo("jaasConfigScram512");
  }

  @Test
  public void testGetSaslSsl_GSSAPIMechanismProperties() {
    String envStr = "localhost:9098";
    String clusterId = "CLID6";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    when(env.getProperty("kafkasasl.saslmechanism.gssapi")).thenReturn("GSSAPI");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.jaasconfig.gssapi"))
        .thenReturn("jaasConfigGssapi");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.saslmechanism.gssapi.servicename"))
        .thenReturn("serviceName");
    var props = clusterApiUtils.getSaslSsl_GSSAPIMechanismProperties(envStr, clusterId);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(envStr);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SASL_SSL");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientsaslsslgssapi");
    assertThat(props.getProperty("sasl.mechanism")).isEqualTo("GSSAPI");
    assertThat(props.getProperty("sasl.jaas.config")).isEqualTo("jaasConfigGssapi");
    assertThat(props.getProperty("sasl.kerberos.service.name")).isEqualTo("serviceName");
  }

  @Test
  public void testGetSasl_OauthBearerMechanismPropertiesSsl() {
    String envStr = "localhost:9099";
    String clusterId = "CLID7";
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("1000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("100");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.saslmechanism.oauthbearer"))
        .thenReturn("OAUTHBEARER");
    when(env.getProperty(
            clusterId.toLowerCase() + ".kafkasasl.sasl.oauthbearer.token.endpoint.url"))
        .thenReturn("tokenUrl");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.sasl.client.callback.handler.class"))
        .thenReturn("callbackHandler");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkasasl.jaasconfig.oauthbearer"))
        .thenReturn("jaasConfigOauth");
    when(env.getProperty(
            clusterId.toLowerCase() + ".kafkasasl.saslmechanism.oauthbearer.servicename"))
        .thenReturn("serviceNameOauth");
    var props = clusterApiUtils.getSasl_OauthBearerMechanismProperties(envStr, clusterId, true);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(envStr);
    assertThat(props.getProperty("security.protocol")).isEqualTo("SASL_SSL");
    assertThat(props.getProperty("client.id")).isEqualTo("klawclientsaslplainoauthbearer");
    assertThat(props.getProperty("sasl.mechanism")).isEqualTo("OAUTHBEARER");
    assertThat(props.getProperty("sasl.oauthbearer.token.endpoint.url")).isEqualTo("tokenUrl");
    assertThat(props.getProperty("sasl.client.callback.handler.class"))
        .isEqualTo("callbackHandler");
    assertThat(props.getProperty("sasl.jaas.config")).isEqualTo("jaasConfigOauth");
  }

  @Test
  public void testGetSslConfig() {
    String clusterId = "CLID8";
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.keystore.certificate.chain"))
        .thenReturn("certChain");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.keystore.key")).thenReturn("key");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.truststore.certificates"))
        .thenReturn("trustCerts");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.keystore.location"))
        .thenReturn("keystoreLoc");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.keystore.pwd"))
        .thenReturn("keystorePwd");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.key.pwd")).thenReturn("keyPwd");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.keystore.type")).thenReturn("PKCS12");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.truststore.type"))
        .thenReturn("PKCS12");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.truststore.location"))
        .thenReturn("truststoreLoc");
    when(env.getProperty(clusterId.toLowerCase() + ".kafkassl.truststore.pwd"))
        .thenReturn("truststorePwd");
    var props = clusterApiUtils.getSslConfig(clusterId);
    assertThat(props.getProperty("ssl.keystore.certificate.chain")).isEqualTo("certChain");
    assertThat(props.getProperty("ssl.keystore.key")).isEqualTo("key");
    assertThat(props.getProperty("ssl.truststore.certificates")).isEqualTo("trustCerts");
    assertThat(props.getProperty("ssl.keystore.location")).isEqualTo("keystoreLoc");
    assertThat(props.getProperty("ssl.keystore.password")).isEqualTo("keystorePwd");
    assertThat(props.getProperty("ssl.key.password")).isEqualTo("keyPwd");
    assertThat(props.getProperty("ssl.keystore.type")).isEqualTo("PKCS12");
    assertThat(props.getProperty("ssl.truststore.type")).isEqualTo("PKCS12");
    assertThat(props.getProperty("ssl.truststore.location")).isEqualTo("truststoreLoc");
    assertThat(props.getProperty("ssl.truststore.password")).isEqualTo("truststorePwd");
    assertThat(props.getProperty("ssl.enabled.protocols")).isEqualTo("TLSv1.2,TLSv1.1");
    assertThat(props.getProperty("ssl.endpoint.identification.algorithm")).isEqualTo("");
  }

  @Test
  public void testSetOtherConfig() {
    Properties props = new Properties();
    when(adminClientProperties.getRetriesConfig()).thenReturn("5");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("2000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("200");
    clusterApiUtils.setOtherConfig(props);
    assertThat(props.getProperty("retries")).isEqualTo("5");
    assertThat(props.getProperty("request.timeout.ms")).isEqualTo("2000");
    assertThat(props.getProperty("retry.backoff.ms")).isEqualTo("200");
  }

  @Test
  public void testCreateHeadersSchemaRegistry() {
    String clusterId = "CLID9";
    String credentials = "user:pass";
    when(env.getProperty(
            clusterId.toLowerCase() + ClusterApiUtils.KAFKA_SR_CREDENTIALS_PROPERTY_SFX))
        .thenReturn(credentials);
    var headers =
        clusterApiUtils.createHeaders(
            clusterId, io.aiven.klaw.clusterapi.models.enums.KafkaClustersType.SCHEMA_REGISTRY);
    assertThat(headers.getFirst("Authorization"))
        .isEqualTo("Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes()));
  }

  @Test
  public void testCreateHeadersKafkaConnect() {
    String clusterId = "CLID10";
    String credentials = "user2:pass2";
    when(env.getProperty(
            clusterId.toLowerCase() + ClusterApiUtils.KAFKA_CONNECT_CREDENTIALS_PROPERTY_SFX))
        .thenReturn(credentials);
    var headers =
        clusterApiUtils.createHeaders(
            clusterId, io.aiven.klaw.clusterapi.models.enums.KafkaClustersType.KAFKA_CONNECT);
    assertThat(headers.getFirst("Authorization"))
        .isEqualTo("Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes()));
  }

  @Test
  public void testCreateHeadersKafka() {
    String clusterId = "CLID11";
    String credentials = "user3:pass3";
    when(env.getProperty(
            clusterId.toLowerCase()
                + ClusterApiUtils.KAFKA_CONFLUENT_CLOUD_CREDENTIALS_PROPERTY_SFX))
        .thenReturn(credentials);
    var headers =
        clusterApiUtils.createHeaders(
            clusterId, io.aiven.klaw.clusterapi.models.enums.KafkaClustersType.KAFKA);
    assertThat(headers.getFirst("Authorization"))
        .isEqualTo("Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes()));
  }
}
