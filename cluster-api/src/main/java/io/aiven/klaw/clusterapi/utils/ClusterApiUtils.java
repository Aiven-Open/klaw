package io.aiven.klaw.clusterapi.utils;

import static io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol.PLAINTEXT;
import static io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol.SSL;

import com.google.common.base.Strings;
import io.aiven.klaw.clusterapi.config.SslContextConfig;
import io.aiven.klaw.clusterapi.models.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ClusterApiUtils {

  public static final String HTTPS_PREFIX = "https://";
  public static final String HTTP_PREFIX = "http://";
  public static final String SHA_256 = "SHA_256";
  public static final String SHA_512 = "SHA_512";
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

  private final AdminClientProperties adminClientProperties;

  @Autowired
  public ClusterApiUtils(Environment env, AdminClientProperties adminClientProperties) {
    this(env, adminClientProperties, new HashMap<>());
  }

  ClusterApiUtils(
      Environment env,
      AdminClientProperties adminClientProperties,
      Map<String, AdminClient> adminClientsMap) {
    this.env = env;
    this.adminClientsMap = adminClientsMap;
    this.adminClientProperties = adminClientProperties;
  }

  //    public void removeSSLElementFromAdminClientMap(String protocol, String clusterName){
  //        log.info("Into removeSSLElementFromAdminClientMap");
  //        String adminClientKeyReq = protocol + clusterName;
  //        List<String> sslKeys = adminClientsMap.keySet().stream()
  //                .filter(adminClientKey -> adminClientKey.equals(adminClientKeyReq))
  //                .collect(Collectors.toList());
  //        sslKeys.forEach(adminClientsMap::remove);
  //    }

  private String getHash(String envHost) {
    return new String(Base64.getEncoder().encode(messageDigest.digest(envHost.getBytes())));
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
      adminClient.listTopics().names().get();
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
      String suffixUrl,
      KafkaSupportedProtocol protocol,
      KafkaClustersType kafkaClustersType,
      String clusterIdentification) {
    RestTemplate restTemplate;
    String connectorsUrl = "";

    if (PLAINTEXT == protocol) {
      connectorsUrl = HTTP_PREFIX + suffixUrl;
      restTemplate = new RestTemplate();
    } else if (SSL == protocol) {
      if (KafkaClustersType.KAFKA_CONNECT.equals(kafkaClustersType)) {
        String connectCredentials =
            env.getProperty(clusterIdentification.toLowerCase() + ".klaw.kafkaconnect.credentials");
        if (connectCredentials != null && !connectCredentials.isBlank()) {
          connectorsUrl = HTTPS_PREFIX + connectCredentials + "@" + suffixUrl;
        } else {
          connectorsUrl = HTTPS_PREFIX + suffixUrl;
        }
      } else if (KafkaClustersType.SCHEMA_REGISTRY.equals(kafkaClustersType)) {
        String srCredentials =
            env.getProperty(
                clusterIdentification.toLowerCase() + ".klaw.schemaregistry.credentials");
        if (srCredentials != null && !srCredentials.isBlank()) {
          connectorsUrl = HTTPS_PREFIX + srCredentials + "@" + suffixUrl;
        } else {
          connectorsUrl = HTTPS_PREFIX + suffixUrl;
        }
      }
      restTemplate = new RestTemplate(SslContextConfig.requestFactory);
    } else {
      restTemplate = new RestTemplate();
    }

    return Pair.of(connectorsUrl, restTemplate);
  }
}
