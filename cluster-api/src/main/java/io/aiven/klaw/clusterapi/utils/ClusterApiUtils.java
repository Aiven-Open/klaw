package io.aiven.klaw.clusterapi.utils;

import static io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol.PLAINTEXT;
import static io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol.SSL;

import com.google.common.base.Strings;
import io.aiven.klaw.clusterapi.config.SslContextConfig;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ClusterApiUtils {

  private static final String PLAIN_REST_TEMPLATE = "PlainRestTemplate";
  private static final String SSL_REST_TEMPLATE = "SSLRestTemplate";
  public static final String HTTPS_PREFIX = "https://";
  public static final String HTTP_PREFIX = "http://";
  public static final String SHA_256 = "SHA_256";
  public static final String SHA_512 = "SHA_512";
  public static final String KAFKA_CONNECT_CREDENTIALS_PROPERTY_SFX =
      ".klaw.kafkaconnect.credentials";

  public static final String KAFKA_SR_CREDENTIALS_PROPERTY_SFX = ".klaw.schemaregistry.credentials";
  public static final String KAFKA_CONFLUENT_CLOUD_CREDENTIALS_PROPERTY_SFX =
      ".klaw.confluentcloud.credentials";

  private static MessageDigest messageDigest;

  static {
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      log.error("Error ", e);
    }
  }

  private final Environment env;
  private final Map<String, AdminClient> adminClientsMap;

  private final Map<String, RestTemplate> restTemplateMap;

  private final AdminClientProperties adminClientProperties;

  @Autowired private SslContextConfig sslContextConfig;

  @Autowired
  public ClusterApiUtils(Environment env, AdminClientProperties adminClientProperties) {
    this(env, adminClientProperties, new HashMap<>(), new HashMap<>());
  }

  public AdminClientProperties getAdminClientProperties() {
    return adminClientProperties;
  }

  ClusterApiUtils(
      Environment env,
      AdminClientProperties adminClientProperties,
      Map<String, AdminClient> adminClientsMap,
      Map<String, RestTemplate> restTemplateMap) {
    this.env = env;
    this.adminClientsMap = adminClientsMap;
    this.adminClientProperties = adminClientProperties;
    this.restTemplateMap = restTemplateMap;
  }

  //    public void removeSSLElementFromAdminClientMap(String protocol, String clusterName){
  //        log.info("Into removeSSLElementFromAdminClientMap");
  //        String adminClientKeyReq = protocol + clusterName;
  //        List<String> sslKeys = adminClientsMap.keySet().stream()
  //                .filter(adminClientKey -> adminClientKey.equals(adminClientKeyReq))
  //                .collect(Collectors.toList());
  //        sslKeys.forEach(adminClientsMap::remove);
  //    }

  public String getHash(String envHost) {
    return new String(Base64.encodeBase64(messageDigest.digest(envHost.getBytes()), false));
  }

  public AdminClient getAdminClient(
      String envHost, KafkaSupportedProtocol protocol, String clusterIdentification)
      throws Exception {
    log.info(
        "Host : {} Protocol {} clusterIdentification {}", envHost, protocol, clusterIdentification);

    AdminClient adminClient = null;
    String adminClientKey = protocol + clusterIdentification + getHash(envHost);

    try {
      switch (protocol) {
        case PLAINTEXT:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient = AdminClient.create(getPlainProperties(envHost));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }
          break;

        case SSL:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient = AdminClient.create(getSslProperties(envHost, clusterIdentification));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }
          break;

        case SASL_PLAIN:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient =
                AdminClient.create(getSaslPlainProperties(envHost, clusterIdentification));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }
          break;

        case SASL_SSL_PLAIN_MECHANISM:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient =
                AdminClient.create(
                    getSaslSsl_PlainMechanismProperties(envHost, clusterIdentification));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }

          break;

        case SASL_SSL_SCRAM_MECHANISM_256:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient =
                AdminClient.create(
                    getSaslSsl_ScramMechanismProperties(envHost, clusterIdentification, SHA_256));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }

          break;

        case SASL_SSL_SCRAM_MECHANISM_512:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient =
                AdminClient.create(
                    getSaslSsl_ScramMechanismProperties(envHost, clusterIdentification, SHA_512));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }

          break;

        case SASL_SSL_GSSAPI_MECHANISM:
          if (!adminClientsMap.containsKey(adminClientKey)) {
            adminClient =
                AdminClient.create(
                    getSaslSsl_GSSAPIMechanismProperties(envHost, clusterIdentification));
          } else {
            adminClient = adminClientsMap.get(adminClientKey);
          }
          break;
      }
    } catch (Exception exception) {
      log.error("Unable to create Admin client ", exception);
      exception.printStackTrace();
      throw new Exception("Cannot connect to cluster. Please contact Administrator.");
    }

    if (adminClient == null) {
      log.error("Cannot create Admin Client {} {}", envHost, protocol);
      throw new Exception("Cannot connect to cluster. Please contact Administrator.");
    }

    try {
      adminClient
          .listTopics()
          .names()
          .get(adminClientProperties.getTopicsTimeoutSecs(), TimeUnit.SECONDS);
      if (!adminClientsMap.containsKey(adminClientKey)) {
        adminClientsMap.put(adminClientKey, adminClient);
      }
      return adminClient;
    } catch (Exception e) {
      adminClientsMap.remove(adminClientKey);
      adminClient.close();
      log.error("Cannot create Admin Client {} {} {}", envHost, protocol, clusterIdentification, e);
      throw new Exception("Cannot connect to cluster. Please contact Administrator.");
    }
  }

  public Properties getPlainProperties(String environment) {
    Properties props = new Properties();

    props.put("bootstrap.servers", environment);
    setOtherConfig(props);

    return props;
  }

  public Properties getSslProperties(String environment, String clusterIdentification) {
    Properties props = getSslConfig(clusterIdentification);

    props.put("bootstrap.servers", environment);
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    props.put(AdminClientConfig.CLIENT_ID_CONFIG, "klawclientssl");
    setOtherConfig(props);

    return props;
  }

  public Properties getSaslPlainProperties(String environment, String clusterIdentification) {
    Properties props = new Properties();

    props.put("bootstrap.servers", environment);

    try {
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
      props.put(AdminClientConfig.CLIENT_ID_CONFIG, "klawclientsaslplain");
      setOtherConfig(props);

      if (!Strings.isNullOrEmpty(env.getProperty("kafkasasl.saslmechanism.plain"))) {
        props.put(SaslConfigs.SASL_MECHANISM, env.getProperty("kafkasasl.saslmechanism.plain"));
      }

      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.plain"))) {
        props.put(
            SaslConfigs.SASL_JAAS_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.plain"));
      }

    } catch (Exception exception) {
      log.error("Error : Cannot set SASL PLAIN Config properties.", exception);
    }

    return props;
  }

  public Properties getSaslSsl_PlainMechanismProperties(
      String environment, String clusterIdentification) {
    Properties props = getSslConfig(clusterIdentification);

    try {
      props.put("bootstrap.servers", environment);
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
      props.put(AdminClientConfig.CLIENT_ID_CONFIG, "klawclientsaslsslplain");
      setOtherConfig(props);

      if (!Strings.isNullOrEmpty(env.getProperty("kafkasasl.saslmechanism.plain"))) {
        props.put(SaslConfigs.SASL_MECHANISM, env.getProperty("kafkasasl.saslmechanism.plain"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.plain"))) {
        props.put(
            SaslConfigs.SASL_JAAS_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.plain"));
      }
    } catch (Exception exception) {
      log.error("Error : Cannot set SASL SSL PLAIN Config properties.", exception);
    }

    return props;
  }

  public Properties getSaslSsl_ScramMechanismProperties(
      String environment, String clusterIdentification, String algorithm) {
    Properties props = getSslConfig(clusterIdentification);

    try {
      props.put("bootstrap.servers", environment);
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
      props.put(AdminClientConfig.CLIENT_ID_CONFIG, "klawclientsaslsslscram");
      setOtherConfig(props);

      if (SHA_256.equals(algorithm)) {
        if (!Strings.isNullOrEmpty(env.getProperty("kafkasasl.saslmechanism.scram.256"))) {
          props.put(
              SaslConfigs.SASL_MECHANISM, env.getProperty("kafkasasl.saslmechanism.scram.256"));
        }
      } else if (SHA_512.equals(algorithm)) {
        if (!Strings.isNullOrEmpty(env.getProperty("kafkasasl.saslmechanism.scram.512"))) {
          props.put(
              SaslConfigs.SASL_MECHANISM, env.getProperty("kafkasasl.saslmechanism.scram.512"));
        }
      }

      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.scram"))) {
        props.put(
            SaslConfigs.SASL_JAAS_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.scram"));
      }
    } catch (Exception exception) {
      log.error("Error : Cannot set SASL SSL SCRAM Config properties.", exception);
    }

    return props;
  }

  public Properties getSaslSsl_GSSAPIMechanismProperties(
      String environment, String clusterIdentification) {
    Properties props = getSslConfig(clusterIdentification);

    try {
      props.put("bootstrap.servers", environment);
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
      props.put(AdminClientConfig.CLIENT_ID_CONFIG, "klawclientsaslsslgssapi");
      setOtherConfig(props);

      if (!Strings.isNullOrEmpty(env.getProperty("kafkasasl.saslmechanism.gssapi"))) {
        props.put(SaslConfigs.SASL_MECHANISM, env.getProperty("kafkasasl.saslmechanism.gssapi"));
      }

      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.gssapi"))) {
        props.put(
            SaslConfigs.SASL_JAAS_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkasasl.jaasconfig.gssapi"));
      }

      if (!Strings.isNullOrEmpty(
          env.getProperty(
              clusterIdentification.toLowerCase()
                  + ".kafkasasl.saslmechanism.gssapi.servicename"))) {
        props.put(
            SaslConfigs.SASL_KERBEROS_SERVICE_NAME,
            env.getProperty(
                clusterIdentification.toLowerCase()
                    + ".kafkasasl.saslmechanism.gssapi.servicename"));
      }
    } catch (Exception exception) {
      log.error("Error : Cannot set SASL SSL GSSAPI Config properties.", exception);
    }

    return props;
  }

  public Properties getSslConfig(String clusterIdentification) {
    Properties props = new Properties();

    try {
      if (!Strings.isNullOrEmpty(
          env.getProperty(
              clusterIdentification.toLowerCase() + ".kafkassl.keystore.certificate.chain"))) {
        props.put(
            SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG,
            env.getProperty(
                clusterIdentification.toLowerCase() + ".kafkassl.keystore.certificate.chain"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.key"))) {
        props.put(
            SslConfigs.SSL_KEYSTORE_KEY_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.key"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(
              clusterIdentification.toLowerCase() + ".kafkassl.truststore.certificates"))) {
        props.put(
            SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG,
            env.getProperty(
                clusterIdentification.toLowerCase() + ".kafkassl.truststore.certificates"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.location"))) {
        props.put(
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.location"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.pwd"))) {
        props.put(
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.pwd"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.key.pwd"))) {
        props.put(
            SslConfigs.SSL_KEY_PASSWORD_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.key.pwd"));
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.type"))) {
        props.put(
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.keystore.type"));
      } else {
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "JKS");
      }
      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.truststore.type"))) {
        props.put(
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.truststore.type"));
      } else {
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
      }

      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.truststore.location"))) {
        props.put(
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.truststore.location"));
      }

      if (!Strings.isNullOrEmpty(
          env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.truststore.pwd"))) {
        props.put(
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
            env.getProperty(clusterIdentification.toLowerCase() + ".kafkassl.truststore.pwd"));
      }

      props.put("ssl.enabled.protocols", "TLSv1.2,TLSv1.1");
      props.put("ssl.endpoint.identification.algorithm", "");
    } catch (Exception exception) {
      log.error("Error : Cannot set SSL Config properties.", exception);
    }

    return props;
  }

  void setOtherConfig(Properties props) {
    props.put(AdminClientConfig.RETRIES_CONFIG, adminClientProperties.getRetriesConfig());
    props.put(
        AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, adminClientProperties.getRequestTimeOutMs());
    props.put(
        AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, adminClientProperties.getRetryBackOffMsConfig());
  }

  public Pair<String, RestTemplate> getRequestDetails(
      String suffixUrl, KafkaSupportedProtocol protocol) {
    String connectorsUrl = "";

    if (PLAINTEXT == protocol) {
      connectorsUrl = HTTP_PREFIX + suffixUrl;
      if (!restTemplateMap.containsKey(PLAIN_REST_TEMPLATE)) {
        restTemplateMap.put(PLAIN_REST_TEMPLATE, new RestTemplate());
      }
      return Pair.of(connectorsUrl, restTemplateMap.get(PLAIN_REST_TEMPLATE));
    } else if (SSL == protocol) {
      connectorsUrl = HTTPS_PREFIX.concat(suffixUrl);
      if (!restTemplateMap.containsKey(SSL_REST_TEMPLATE)) {
        restTemplateMap.put(
            SSL_REST_TEMPLATE, new RestTemplate(sslContextConfig.getClientHttpRequestFactory()));
      }
      return Pair.of(connectorsUrl, restTemplateMap.get(SSL_REST_TEMPLATE));
    }
    return Pair.of(connectorsUrl, new RestTemplate());
  }

  public HttpHeaders createHeaders(
      String clusterIdentification, KafkaClustersType kafkaClustersType) {
    String credentials = null;
    if (KafkaClustersType.SCHEMA_REGISTRY.equals(kafkaClustersType)) {
      credentials =
          env.getProperty(
              clusterIdentification.toLowerCase().concat(KAFKA_SR_CREDENTIALS_PROPERTY_SFX));
    } else if (KafkaClustersType.KAFKA_CONNECT.equals(kafkaClustersType)) {
      credentials =
          env.getProperty(
              clusterIdentification.toLowerCase().concat(KAFKA_CONNECT_CREDENTIALS_PROPERTY_SFX));
    } else if (KafkaClustersType.KAFKA.equals(kafkaClustersType)) {
      credentials =
          env.getProperty(
              clusterIdentification
                  .toLowerCase()
                  .concat(KAFKA_CONFLUENT_CLOUD_CREDENTIALS_PROPERTY_SFX));
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    if (credentials != null) {
      byte[] encodedAuth = Base64.encodeBase64(credentials.getBytes(), false);
      String authHeader = "Basic " + new String(encodedAuth);
      httpHeaders.set("Authorization", authHeader);
    }

    return httpHeaders;
  }
}
