package io.aiven.klaw.service;

import static io.aiven.klaw.service.KwConstants.CLUSTER_CONN_URL_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.AclPatternType;
import io.aiven.klaw.model.AclType;
import io.aiven.klaw.model.AclsNativeType;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.ClusterStatus;
import io.aiven.klaw.model.KafkaClustersType;
import io.aiven.klaw.model.KafkaFlavors;
import io.aiven.klaw.model.RequestOperationType;
import io.aiven.klaw.model.cluster.ClusterAclRequest;
import io.aiven.klaw.model.cluster.ClusterConnectorRequest;
import io.aiven.klaw.model.cluster.ClusterSchemaRequest;
import io.aiven.klaw.model.cluster.ClusterTopicRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ClusterApiService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String URL_DELIMITER = "/";
  @Autowired ManageDatabase manageDatabase;

  @Value("${server.ssl.trust-store:null}")
  private String trustStore;

  @Value("${server.ssl.trust-store-password:null}")
  private String trustStorePwd;

  @Value("${server.ssl.key-store:null}")
  private String keyStore;

  @Value("${server.ssl.key-store-password:null}")
  private String keyStorePwd;

  @Value("${server.ssl.key-store-type:JKS}")
  private String keyStoreType;

  protected static HttpComponentsClientHttpRequestFactory requestFactory;

  static String URI_CREATE_TOPICS = "/topics/createTopics";
  static String URI_UPDATE_TOPICS = "/topics/updateTopics";
  static String URI_DELETE_TOPICS = "/topics/deleteTopics";

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.clusterapi.access.username}")
  private String clusterApiUser;

  @Value("${klaw.clusterapi.access.password}")
  private String clusterApiPwd;

  private static String clusterConnUrl;

  public ClusterApiService(ManageDatabase manageDatabase) {
    this.manageDatabase = manageDatabase;
  }

  RestTemplate httpRestTemplate, httpsRestTemplate;

  private RestTemplate getRestTemplate() {
    if (clusterConnUrl.toLowerCase().startsWith("https")) {
      if (this.httpsRestTemplate == null) {
        this.httpsRestTemplate = new RestTemplate(requestFactory);
      }
      return this.httpsRestTemplate;
    } else {
      if (this.httpRestTemplate == null) {
        this.httpRestTemplate = new RestTemplate();
      }
      return this.httpRestTemplate;
    }
  }

  private void getClusterApiProperties(int tenantId) {
    clusterConnUrl = manageDatabase.getKwPropertyValue(CLUSTER_CONN_URL_KEY, tenantId);
  }

  public String getClusterApiStatus(String clusterApiUrl, boolean testConnection, int tenantId) {
    log.info(
        "getClusterApiStatus clusterApiUrl {} testConnection{}", clusterApiUrl, testConnection);
    getClusterApiProperties(tenantId);
    try {
      String uriClusterApiStatus = "/topics/getApiStatus";
      String uri;
      if (testConnection) {
        uri = clusterApiUrl + uriClusterApiStatus;
      } else {
        uri = clusterConnUrl + uriClusterApiStatus; // from stored kw props
      }

      ResponseEntity<ClusterStatus> resultBody =
          getRestTemplate().exchange(uri, HttpMethod.GET, getHttpEntity(), ClusterStatus.class);
      return Objects.requireNonNull(resultBody.getBody()).value;
    } catch (Exception e) {
      log.error("Error from getClusterApiStatus ", e);
      return ClusterStatus.OFFLINE.value;
    }
  }

  String getSchemaClusterStatus(String host, int tenantId) {
    log.info("getSchemaClusterStatus {}", host);
    getClusterApiProperties(tenantId);
    String clusterStatus;
    try {
      String uri = host + "/subjects";
      ResponseEntity<ClusterStatus> resultBody =
          getRestTemplate().exchange(uri, HttpMethod.GET, getHttpEntity(), ClusterStatus.class);
      return Objects.requireNonNull(resultBody.getBody()).value;
    } catch (Exception e) {
      log.error("Error from getSchemaClusterStatus ", e);
      return ClusterStatus.OFFLINE.value;
    }
  }

  String getKafkaClusterStatus(
      String bootstrapHost,
      String protocol,
      String clusterIdentification,
      String clusterType,
      int tenantId) {
    log.debug("getKafkaClusterStatus {} {}", bootstrapHost, protocol);
    getClusterApiProperties(tenantId);

    try {
      String uriEnvStatus = "/topics/getStatus/";
      String uri =
          clusterConnUrl
              + uriEnvStatus
              + bootstrapHost
              + URL_DELIMITER
              + String.join(URL_DELIMITER, protocol, clusterIdentification, clusterType);

      ResponseEntity<ClusterStatus> resultBody =
          getRestTemplate().exchange(uri, HttpMethod.GET, getHttpEntity(), ClusterStatus.class);
      return Objects.requireNonNull(resultBody.getBody()).value;
    } catch (Exception e) {
      log.error("Error from getKafkaClusterStatus ", e);
      return ClusterStatus.NOT_KNOWN.value;
    }
  }

  public List<Map<String, String>> getConsumerOffsets(
      String bootstrapHost,
      String protocol,
      String clusterIdentification,
      String topic,
      String consumerGroupId,
      int tenantId)
      throws KlawException {
    log.info("getConsumerOffsets {} {} {} {}", bootstrapHost, protocol, topic, consumerGroupId);
    getClusterApiProperties(tenantId);
    List<Map<String, String>> offsetsMap;
    try {
      String url = "/topics/getConsumerOffsets/";
      url =
          clusterConnUrl
              + url
              + bootstrapHost
              + URL_DELIMITER
              + String.join(URL_DELIMITER, protocol, clusterIdentification, consumerGroupId, topic);

      ResponseEntity<List<Map<String, String>>> resultBody =
          getRestTemplate()
              .exchange(
                  url, HttpMethod.GET, getHttpEntity(), new ParameterizedTypeReference<>() {});

      offsetsMap = new ArrayList<>(Objects.requireNonNull(resultBody.getBody()));
    } catch (Exception e) {
      log.error("Error from getConsumerOffsets ", e);
      throw new KlawException("Could not get consumer offsets");
    }
    return offsetsMap;
  }

  public Map<String, String> getTopicEvents(
      String bootstrapHost,
      String protocol,
      String clusterIdentification,
      String topic,
      String offsetId,
      String consumerGroupId,
      int tenantId)
      throws KlawException {
    log.info(
        "getTopicEvents {} {} {} {} {}", bootstrapHost, protocol, topic, offsetId, consumerGroupId);
    getClusterApiProperties(tenantId);
    Map<String, String> eventsMap;
    try {
      String url = "/topics/getTopicContents/";
      url =
          clusterConnUrl
              + url
              + bootstrapHost
              + URL_DELIMITER
              + String.join(
                  URL_DELIMITER, protocol, clusterIdentification, consumerGroupId, topic, offsetId);

      ResponseEntity<Map<String, String>> resultBody =
          getRestTemplate()
              .exchange(
                  url, HttpMethod.GET, getHttpEntity(), new ParameterizedTypeReference<>() {});

      eventsMap = new TreeMap<>(Objects.requireNonNull(resultBody.getBody()));
    } catch (Exception e) {
      log.error("Error from getTopicEvents {} ", topic, e);
      throw new KlawException("Could not get events for Topic " + topic);
    }
    return eventsMap;
  }

  public List<Map<String, String>> getAcls(
      String bootstrapHost, Env envSelected, String protocol, int tenantId) throws KlawException {
    log.info("getAcls {} {} {}", bootstrapHost, protocol, tenantId);
    getClusterApiProperties(tenantId);

    List<Map<String, String>> aclListOriginal;
    try {
      String uriGetAcls = "/topics/getAcls/";
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA.value, tenantId)
              .get(envSelected.getClusterId());

      String uri;
      // aiven config
      if (KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value.equals(kwClusters.getKafkaFlavor())) {
        uri =
            clusterConnUrl
                + uriGetAcls
                + bootstrapHost
                + URL_DELIMITER
                + String.join(
                    URL_DELIMITER,
                    AclsNativeType.AIVEN.name(),
                    protocol,
                    kwClusters.getClusterName() + kwClusters.getClusterId(),
                    kwClusters.getProjectName(),
                    kwClusters.getServiceName());
      } else {
        uri =
            clusterConnUrl
                + uriGetAcls
                + bootstrapHost
                + URL_DELIMITER
                + String.join(
                    URL_DELIMITER,
                    AclsNativeType.NATIVE.name(),
                    protocol,
                    kwClusters.getClusterName() + kwClusters.getClusterId(),
                    "na",
                    "na");
      }

      ResponseEntity<Set<Map<String, String>>> resultBody =
          getRestTemplate()
              .exchange(
                  uri, HttpMethod.GET, getHttpEntity(), new ParameterizedTypeReference<>() {});
      aclListOriginal = new ArrayList<>(Objects.requireNonNull(resultBody.getBody()));
    } catch (Exception e) {
      log.error("Error from getAcls", e);
      throw new KlawException("Could not load topics/acls. Please contact Administrator.");
    }
    return aclListOriginal;
  }

  public List<Map<String, String>> getAllTopics(
      String bootstrapHost, String protocol, String clusterIdentification, int tenantId)
      throws Exception {
    log.info("getAllTopics {} {}", bootstrapHost, protocol);
    getClusterApiProperties(tenantId);
    List<Map<String, String>> topicsList;
    try {
      String uriGetTopics = "/topics/getTopics/";
      String uriGetTopicsFull =
          clusterConnUrl
              + uriGetTopics
              + bootstrapHost
              + URL_DELIMITER
              + String.join(URL_DELIMITER, protocol, clusterIdentification);

      HttpEntity<String> entity = getHttpEntity();
      ResponseEntity<Set<Map<String, String>>> s =
          getRestTemplate()
              .exchange(
                  uriGetTopicsFull, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
      topicsList = new ArrayList<>(Objects.requireNonNull(s.getBody()));
    } catch (Exception e) {
      log.error("Error from getAllTopics", e);
      throw new KlawException("Could not load topics. Please contact Administrator.");
    }

    return topicsList;
  }

  public String approveConnectorRequests(
      String connectorName,
      String protocol,
      String connectorType,
      String connectorConfig,
      String kafkaConnectHost,
      int tenantId)
      throws KlawException {
    log.info("approveConnectorRequests {} {}", connectorConfig, kafkaConnectHost);
    getClusterApiProperties(tenantId);
    ResponseEntity<Map<String, String>> response;
    try {
      ClusterConnectorRequest clusterConnectorRequest =
          ClusterConnectorRequest.builder()
              .env(kafkaConnectHost)
              .connectorName(connectorName)
              .connectorConfig(connectorConfig)
              .protocol(protocol)
              .build();

      String uri;
      String uriGetTopics = "/topics/";

      if (RequestOperationType.CREATE.value.equals(connectorType)) {
        uri = clusterConnUrl + uriGetTopics + "postConnector";
      } else if (RequestOperationType.UPDATE.value.equals(connectorType)) {
        uri = clusterConnUrl + uriGetTopics + "updateConnector";
      } else {
        uri = clusterConnUrl + uriGetTopics + "deleteConnector";
      }

      HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<ClusterConnectorRequest> request =
          new HttpEntity<>(clusterConnectorRequest, headers);
      response =
          getRestTemplate()
              .exchange(uri, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});

      if (ApiResultStatus.SUCCESS.value.equals(
          Objects.requireNonNull(response.getBody()).get("result"))) {
        return ApiResultStatus.SUCCESS.value;
      } else {
        return response.getBody().get("errorText");
      }

    } catch (Exception e) {
      log.error("approveConnectorRequests {} ", connectorName, e);
      throw new KlawException("Could not approve connector request. Please contact Administrator.");
    }
  }

  public ResponseEntity<ApiResponse> approveTopicRequests(
      String topicName,
      String topicRequestType,
      int topicPartitions,
      String replicationFactor,
      String topicEnvId,
      int tenantId)
      throws KlawException {
    log.info("approveTopicRequests {} {}", topicName, topicEnvId);
    getClusterApiProperties(tenantId);
    ResponseEntity<ApiResponse> response;
    ClusterTopicRequest clusterTopicRequest;
    try {
      Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(topicEnvId, tenantId);
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA.value, tenantId)
              .get(envSelected.getClusterId());
      clusterTopicRequest =
          ClusterTopicRequest.builder()
              .env(kwClusters.getBootstrapServers())
              .protocol(kwClusters.getProtocol())
              .clusterName(kwClusters.getClusterName() + kwClusters.getClusterId())
              .topicName(topicName)
              .build();

      String uri;
      if (RequestOperationType.CREATE.value.equals(topicRequestType)) {
        uri = clusterConnUrl + URI_CREATE_TOPICS;
        clusterTopicRequest =
            clusterTopicRequest.toBuilder()
                .partitions(topicPartitions)
                .replicationFactor(Short.parseShort(replicationFactor))
                .build();
      } else if (RequestOperationType.UPDATE.value.equals(topicRequestType)) {
        uri = clusterConnUrl + URI_UPDATE_TOPICS;
        clusterTopicRequest =
            clusterTopicRequest.toBuilder()
                .partitions(topicPartitions)
                .replicationFactor(Short.parseShort(replicationFactor))
                .build();
      } else {
        uri = clusterConnUrl + URI_DELETE_TOPICS;
      }

      HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<ClusterTopicRequest> request = new HttpEntity<>(clusterTopicRequest, headers);
      response = getRestTemplate().postForEntity(uri, request, ApiResponse.class);
    } catch (Exception e) {
      log.error("approveTopicRequests {}", topicName, e);
      throw new KlawException("Could not approve topic request. Please contact Administrator.");
    }
    return response;
  }

  public ResponseEntity<ApiResponse> approveAclRequests(AclRequests aclReq, int tenantId)
      throws KlawException {
    log.info("approveAclRequests {}", aclReq);
    getClusterApiProperties(tenantId);
    ResponseEntity<ApiResponse> response;

    try {
      String env = aclReq.getEnvironment();
      String uri;
      String uriCreateAcls = "/topics/createAcls";
      String uriDeleteAcls = "/topics/deleteAcls";

      ClusterAclRequest clusterAclRequest;
      Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(env, tenantId);
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.KAFKA.value, tenantId)
              .get(envSelected.getClusterId());

      // aiven config
      if (Objects.equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value, kwClusters.getKafkaFlavor())) {
        clusterAclRequest =
            ClusterAclRequest.builder()
                .aclNativeType(AclsNativeType.AIVEN.name())
                .projectName(kwClusters.getProjectName())
                .serviceName(kwClusters.getServiceName())
                .topicName(aclReq.getTopicname())
                .username(aclReq.getAcl_ssl())
                .build();

        if (Objects.equals(aclReq.getTopictype(), AclType.PRODUCER.value)) {
          clusterAclRequest = clusterAclRequest.toBuilder().permission("write").build();
        } else {
          clusterAclRequest = clusterAclRequest.toBuilder().permission("read").build();
        }

        if (Objects.equals(RequestOperationType.DELETE.value, aclReq.getAclType())
            && null != aclReq.getJsonParams()) {
          Map<String, String> jsonObj = OBJECT_MAPPER.readValue(aclReq.getJsonParams(), Map.class);
          String aivenAclKey = "aivenaclid";
          if (jsonObj.containsKey(aivenAclKey)) {
            clusterAclRequest =
                clusterAclRequest.toBuilder().aivenAclKey(jsonObj.get(aivenAclKey)).build();
          } else {
            log.error("Error from approveAclRequests : AclId - aivenaclid not found");
            throw new KlawException(
                "Could not approve acl request. AclId - Aiven acl id not found.");
          }
        }
      } else {
        String aclPatternType = aclReq.getAclPatternType();
        clusterAclRequest =
            ClusterAclRequest.builder()
                .aclNativeType(AclsNativeType.NATIVE.name())
                .env(kwClusters.getBootstrapServers())
                .protocol(kwClusters.getProtocol())
                .clusterName(kwClusters.getClusterName() + kwClusters.getClusterId())
                .topicName(aclReq.getTopicname())
                .consumerGroup(aclReq.getConsumergroup())
                .aclType(aclReq.getTopictype())
                .aclIp(aclReq.getAcl_ip())
                .aclSsl(aclReq.getAcl_ssl())
                .transactionalId(aclReq.getTransactionalId())
                .aclIpPrincipleType(aclReq.getAclIpPrincipleType().name())
                .isPrefixAcl(AclPatternType.PREFIXED.value.equals(aclPatternType))
                .build();
      }

      if (RequestOperationType.CREATE.value.equals(aclReq.getAclType())) {
        uri = clusterConnUrl + uriCreateAcls;
        clusterAclRequest =
            clusterAclRequest.toBuilder().requestOperationType(RequestOperationType.CREATE).build();
      } else {
        uri = clusterConnUrl + uriDeleteAcls;
        clusterAclRequest =
            clusterAclRequest.toBuilder().requestOperationType(RequestOperationType.DELETE).build();
      }

      HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<ClusterAclRequest> request = new HttpEntity<>(clusterAclRequest, headers);
      response =
          getRestTemplate()
              .exchange(uri, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
      return response;
    } catch (Exception e) {
      log.error("Error from approveAclRequests", e);
      throw new KlawException("Could not approve acl request. Please contact Administrator.");
    }
  }

  ResponseEntity<ApiResponse> postSchema(
      SchemaRequest schemaRequest, String env, String topicName, int tenantId)
      throws KlawException {
    log.info("postSchema {} {}", topicName, env);
    getClusterApiProperties(tenantId);
    ResponseEntity<ApiResponse> response;
    try {
      String uriPostSchema = "/topics/postSchema";
      String uri = clusterConnUrl + uriPostSchema;

      Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(env, tenantId);
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.SCHEMA_REGISTRY.value, tenantId)
              .get(envSelected.getClusterId());
      ClusterSchemaRequest clusterSchemaRequest =
          ClusterSchemaRequest.builder()
              .protocol(kwClusters.getProtocol())
              .env(kwClusters.getBootstrapServers())
              .topicName(topicName)
              .fullSchema(schemaRequest.getSchemafull())
              .build();

      HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<ClusterSchemaRequest> request = new HttpEntity<>(clusterSchemaRequest, headers);
      response = getRestTemplate().postForEntity(uri, request, ApiResponse.class);
    } catch (Exception e) {
      log.error("Error from postSchema ", e);
      throw new KlawException("Could not post schema. Please contact Administrator.");
    }
    return response;
  }

  public TreeMap<Integer, Map<String, Object>> getAvroSchema(
      String schemaRegistryHost,
      String protocol,
      String clusterName,
      String topicName,
      int tenantId)
      throws Exception {
    log.info("getAvroSchema {} {}", schemaRegistryHost, topicName);
    getClusterApiProperties(tenantId);
    TreeMap<Integer, Map<String, Object>> allVersionSchemas =
        new TreeMap<>(Collections.reverseOrder());
    try {
      String uriGetSchema = "/topics/getSchema/";
      String uriGetTopicsFull =
          clusterConnUrl
              + uriGetSchema
              + schemaRegistryHost
              + URL_DELIMITER
              + String.join(URL_DELIMITER, protocol, clusterName, topicName);

      ResponseEntity<TreeMap<String, Map<String, Object>>> treeMapResponseEntity =
          getRestTemplate()
              .exchange(
                  uriGetTopicsFull,
                  HttpMethod.GET,
                  getHttpEntity(),
                  new ParameterizedTypeReference<>() {});

      for (String schemaVersion :
          Objects.requireNonNull(treeMapResponseEntity.getBody()).keySet()) {
        allVersionSchemas.put(
            Integer.parseInt(schemaVersion), treeMapResponseEntity.getBody().get(schemaVersion));
      }

      return allVersionSchemas;
    } catch (Exception e) {
      log.error("Error from getAvroSchema ", e);
      throw new KlawException("Could not get schema.");
    }
  }

  public Map<String, Object> getConnectorDetails(
      String connectorName, String kafkaConnectHost, String protocol, int tenantId)
      throws KlawException {
    log.info("getConnectorDetails {} {}", connectorName, kafkaConnectHost);
    getClusterApiProperties(tenantId);
    try {
      String uriGetTopics =
          String.join(
              URL_DELIMITER,
              "/topics/getConnectorDetails",
              connectorName,
              kafkaConnectHost,
              protocol);
      String uriGetConnectorsFull = clusterConnUrl + uriGetTopics;

      ResponseEntity<LinkedHashMap<String, Object>> s =
          getRestTemplate()
              .exchange(
                  uriGetConnectorsFull,
                  HttpMethod.GET,
                  getHttpEntity(),
                  new ParameterizedTypeReference<>() {});

      return s.getBody();
    } catch (Exception e) {
      log.error("Error from getConnectorDetails ", e);
      throw new KlawException("Could not get Connector Details." + connectorName);
    }
  }

  public ArrayList<String> getAllKafkaConnectors(String kafkaConnectHost, int tenantId)
      throws KlawException {
    log.info("getAllKafkaConnectors {}", kafkaConnectHost);
    getClusterApiProperties(tenantId);
    try {
      String uriGetTopics = "/topics/getAllConnectors/" + kafkaConnectHost;
      String uriGetConnectorsFull = clusterConnUrl + uriGetTopics;

      ResponseEntity<ArrayList<String>> s =
          getRestTemplate()
              .exchange(
                  uriGetConnectorsFull,
                  HttpMethod.GET,
                  getHttpEntity(),
                  new ParameterizedTypeReference<>() {});

      return s.getBody();
    } catch (Exception e) {
      log.error("Error from getAllKafkaConnectors ", e);
      throw new KlawException("Could not get KafkaConnectors.");
    }
  }

  public Map<String, String> retrieveMetrics(String jmxUrl, String objectName)
      throws KlawException {
    log.info("retrieveMetrics {} {}", jmxUrl, objectName);
    getClusterApiProperties(101);
    try {
      String uriGetTopics = "/metrics/getMetrics";
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("jmxUrl", jmxUrl);
      params.add("objectName", objectName);

      String uriGetTopicsFull = clusterConnUrl + uriGetTopics;
      RestTemplate restTemplate = getRestTemplate();

      HttpHeaders headers =
          createHeaders(clusterApiUser, clusterApiPwd); // createHeaders("user1", "pwd");
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

      ResponseEntity<HashMap<String, String>> s =
          restTemplate.exchange(
              uriGetTopicsFull, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

      return Objects.requireNonNull(s.getBody());

    } catch (Exception e) {
      log.error("Error from  retrieveMetrics {} ", jmxUrl, e);
      throw new KlawException("Could not get metrics.");
    }
  }

  private HttpHeaders createHeaders(String username, String password) {
    HttpHeaders httpHeaders = new HttpHeaders();
    String auth = username + ":" + decodePwd(password);
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
    String authHeader = "Basic " + new String(encodedAuth);
    httpHeaders.set("Authorization", authHeader);

    return httpHeaders;
  }

  // to connect to cluster api if https
  @PostConstruct
  private void setKwSSLContext() {
    if (keyStore != null && !keyStore.equals("null")) {
      TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
      javax.net.ssl.SSLContext sslContext = null;
      try {
        sslContext =
            org.apache.http.ssl.SSLContexts.custom()
                .loadKeyMaterial(getStore(keyStorePwd, keyStore), keyStorePwd.toCharArray())
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
      } catch (NoSuchAlgorithmException
          | KeyManagementException
          | KeyStoreException
          | CertificateException
          | UnrecoverableKeyException
          | IOException e) {
        log.error("Exception: ", e);
      }
      SSLConnectionSocketFactory csf = null;
      if (sslContext != null) {
        csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
      }
      CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
      requestFactory = new HttpComponentsClientHttpRequestFactory();
      requestFactory.setHttpClient(httpClient);
    }
  }

  protected KeyStore getStore(String secret, String storeLoc)
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

    File key = ResourceUtils.getFile(storeLoc);
    final KeyStore store = KeyStore.getInstance(keyStoreType);
    try (InputStream inputStream = new FileInputStream(key)) {
      store.load(inputStream, secret.toCharArray());
    }
    return store;
  }

  private String decodePwd(String pwd) {
    if (pwd != null) {
      BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
      textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

      return textEncryptor.decrypt(pwd);
    }
    return "";
  }

  private HttpEntity<String> getHttpEntity() {
    HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
    headers.setContentType(MediaType.APPLICATION_JSON);

    headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
    return new HttpEntity<>(headers);
  }
}
