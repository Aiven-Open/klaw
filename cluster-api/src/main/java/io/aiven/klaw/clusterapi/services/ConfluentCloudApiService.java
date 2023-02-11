package io.aiven.klaw.clusterapi.services;

import com.google.common.base.Strings;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.confluentcloud.Config;
import io.aiven.klaw.clusterapi.models.confluentcloud.ConfluentCloudAclObject;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListAclsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListTopicsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.TopicCreateRequest;
import io.aiven.klaw.clusterapi.models.confluentcloud.TopicObject;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.enums.AclPatternType;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.RequestOperationType;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ConfluentCloudApiService { // implements InitializingBean {

  public static final String HTTPS_PREFIX = "https://";
  @Autowired private Environment env;
  @Autowired ClusterApiUtils clusterApiUtils;

  public void getAclsTopics() throws Exception {

    String env = "confluent.cloud:443";
    String clusterId = "cc1";
    Map<String, String> configs = new HashMap<>();
    configs.put("cleanup.policy", "delete");
    configs.put("compression.type", "gzip");
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .aclNativeType(AclsNativeType.CONFLUENT_CLOUD)
            .protocol(KafkaSupportedProtocol.SSL)
            .replicationFactor(Short.parseShort("3"))
            .partitions(1)
            .env(env)
            .topicName("testtopic2")
            .clusterName(clusterId)
            .advancedTopicConfiguration(configs)
            .build();
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .aclNativeType(AclsNativeType.CONFLUENT_CLOUD.name())
            .env("confluenthost:443")
            .protocol(KafkaSupportedProtocol.SSL)
            .clusterName("cc1")
            .topicName("testtopic")
            .consumerGroup(null)
            .aclType(AclType.PRODUCER.value)
            .aclIp("11.34.12.34")
            .aclSsl("")
            .transactionalId("")
            .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
            .isPrefixAcl(false)
            .requestOperationType(RequestOperationType.CREATE)
            .build();

    //    createTopic(clusterTopicRequest);
    //    listTopics(env, KafkaSupportedProtocol.SSL, "cc1");
    //    deleteTopic(clusterTopicRequest);
    //    createAcls(clusterAclRequest);
    //    listAcls("confluenthost:443", KafkaSupportedProtocol.SSL, "cc1");
  }

  public Set<Map<String, String>> listTopics(
      String restApiHost, KafkaSupportedProtocol protocol, String clusterIdentification)
      throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("loadTopics {} {} {}", restApiHost, protocol, clusterIdentification);

    String listTopicsUri =
        env.getProperty(
            clusterIdentification.toLowerCase() + ".klaw.clusters.counfluentcloud.listtopics.api");

    if (null == listTopicsUri) {
      log.error("Exception: {}", "listTopics api/uri for confluent cloud is not configured");
      throw new Exception("List topics api/uri for confluent cloud is not configured");
    }

    if (!Strings.isNullOrEmpty(listTopicsUri)) {
      listTopicsUri = restApiHost + listTopicsUri;
      if (KafkaSupportedProtocol.SSL == protocol) {
        listTopicsUri = HTTPS_PREFIX + listTopicsUri;
      }
    }

    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.KAFKA);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<ListTopicsResponse> responseEntity =
          restTemplate.exchange(
              listTopicsUri, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

      List<Map<String, String>> topicsListUpdated = processListTopicsResponse(responseEntity);

      return new HashSet<>(topicsListUpdated);
    } catch (RestClientException e) {
      log.error("Exception:", e);
      throw new Exception("Error in listing topics : " + e.getMessage());
    }
  }

  public Set<Map<String, String>> listAcls(
      String restApiHost, @Valid KafkaSupportedProtocol protocol, String clusterIdentification)
      throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("listAcls {} {} {}", restApiHost, protocol, clusterIdentification);

    String listAclsUri =
        env.getProperty(
            clusterIdentification.toLowerCase() + ".klaw.clusters.counfluentcloud.listacls.api");

    if (null == listAclsUri) {
      log.error("Exception: {}", "listAcls api/uri for confluent cloud is not configured");
      throw new Exception("ListAcls api/uri for confluent cloud is not configured");
    }

    if (!Strings.isNullOrEmpty(listAclsUri)) {
      listAclsUri = restApiHost + listAclsUri;
      if (KafkaSupportedProtocol.SSL == protocol) {
        listAclsUri = HTTPS_PREFIX + listAclsUri;
      }
    }

    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.KAFKA);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<ListAclsResponse> responseEntity =
          restTemplate.exchange(
              listAclsUri, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

      List<Map<String, String>> aclsListUpdated = processListAclsResponse(responseEntity);

      return new HashSet<>(aclsListUpdated);
    } catch (RestClientException e) {
      log.error("Exception:", e);
      throw new Exception("Error in listing acls : " + e.getMessage());
    }
  }

  public Map<String, String> createAcls(@Valid ClusterAclRequest clusterAclRequest)
      throws Exception {
    Map<String, String> resultMap = new HashMap<>();
    RestTemplate restTemplate = getRestTemplate();
    String createAclsUri =
        env.getProperty(
            clusterAclRequest.getClusterName().toLowerCase()
                + ".klaw.clusters.counfluentcloud.listacls.api");

    if (null == createAclsUri) {
      log.error("Exception: {}", "createAcls api/uri for confluent cloud is not configured");
      throw new Exception("CreateAcls api/uri for confluent cloud is not configured");
    }

    if (!Strings.isNullOrEmpty(createAclsUri)) {
      createAclsUri = clusterAclRequest.getEnv() + createAclsUri;
      if (KafkaSupportedProtocol.SSL == clusterAclRequest.getProtocol()) {
        createAclsUri = HTTPS_PREFIX + createAclsUri;
      }
    }
    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterAclRequest.getClusterName(), KafkaClustersType.KAFKA);

    Map<String, String> aclMap = new HashMap<>();
    if (AclType.PRODUCER.value.equals(clusterAclRequest.getAclType())) {
      aclMap.put("operation", "WRITE");
      aclMap.put("resource_type", "TOPIC");
      aclMap.put("resource_name", clusterAclRequest.getTopicName());

      updateAclMap(clusterAclRequest, aclMap);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(aclMap, headers);

      createAclsPostEntity(restTemplate, createAclsUri, request);
    } else {
      aclMap.put("resource_type", "GROUP");
      aclMap.put("operation", "READ");
      aclMap.put("resource_name", clusterAclRequest.getConsumerGroup());

      updateAclMap(clusterAclRequest, aclMap);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(aclMap, headers);
      createAclsPostEntity(restTemplate, createAclsUri, request);

      aclMap = new HashMap<>();
      aclMap.put("operation", "READ");
      aclMap.put("resource_type", "TOPIC");
      aclMap.put("resource_name", clusterAclRequest.getTopicName());
      updateAclMap(clusterAclRequest, aclMap);
      request = new HttpEntity<>(aclMap, headers);
      createAclsPostEntity(restTemplate, createAclsUri, request);
    }

    return resultMap;
  }

  private static void createAclsPostEntity(
      RestTemplate restTemplate, String createAclsUri, HttpEntity<Map<String, String>> request) {
    try {
      restTemplate.postForEntity(createAclsUri, request, String.class);
    } catch (Exception e) {
      log.error("Exception ");
    }
  }

  public String deleteAcls(ClusterAclRequest clusterAclRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("deleteAcls {}", clusterAclRequest);

    String deleteAclsUri =
        env.getProperty(
            clusterAclRequest.getClusterName().toLowerCase()
                + ".klaw.clusters.counfluentcloud.listacls.api");

    if (Strings.isNullOrEmpty(deleteAclsUri)) {
      log.error("Exception: {}", "deleteAcls api/uri for confluent cloud is not configured");
      throw new Exception("Delete acls api/uri for confluent cloud is not configured");
    }

    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterAclRequest.getClusterName(), KafkaClustersType.KAFKA);
    HttpEntity<String> request = new HttpEntity<>(headers);

    if (clusterAclRequest.getAclType().equals(AclType.PRODUCER.value)) {
      deleteAclsUri =
          clusterAclRequest.getEnv()
              + deleteAclsUri
              + "?"
              + String.join(
                  "&",
                  "resource_type=" + clusterAclRequest.getAclType(),
                  "resource_name",
                  "resource_name",
                  "pattern_type",
                  "pattern_type",
                  "principal",
                  "principal",
                  "host",
                  "host",
                  "operation",
                  "operation",
                  "permission",
                  "permission");
      if (KafkaSupportedProtocol.SSL == clusterAclRequest.getProtocol()) {
        deleteAclsUri = HTTPS_PREFIX + deleteAclsUri;
      }

      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);
    } else {
      // consumer group read access
      deleteAclsUri =
          clusterAclRequest.getEnv()
              + deleteAclsUri
              + "?"
              + String.join(
                  "&",
                  "resource_type=" + clusterAclRequest.getAclType(),
                  "resource_name",
                  "resource_name",
                  "pattern_type",
                  "pattern_type",
                  "principal",
                  "principal",
                  "host",
                  "host",
                  "operation",
                  "operation",
                  "permission",
                  "permission");
      if (KafkaSupportedProtocol.SSL == clusterAclRequest.getProtocol()) {
        deleteAclsUri = HTTPS_PREFIX + deleteAclsUri;
      }

      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);

      // read topic access
      deleteAclsUri =
          clusterAclRequest.getEnv()
              + deleteAclsUri
              + "?"
              + String.join(
                  "&",
                  "resource_type=" + clusterAclRequest.getAclType(),
                  "resource_name",
                  "resource_name",
                  "pattern_type",
                  "pattern_type",
                  "principal",
                  "principal",
                  "host",
                  "host",
                  "operation",
                  "operation",
                  "permission",
                  "permission");
      if (KafkaSupportedProtocol.SSL == clusterAclRequest.getProtocol()) {
        deleteAclsUri = HTTPS_PREFIX + deleteAclsUri;
      }

      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);
    }

    return ApiResultStatus.SUCCESS.value;
  }

  private String deleteAclsRestCall(
      RestTemplate restTemplate,
      String deleteAclsUri,
      ClusterAclRequest clusterAclRequest,
      HttpEntity<String> request) {
    try {
      restTemplate.exchange(deleteAclsUri, HttpMethod.DELETE, request, String.class);
    } catch (Exception e) {
      log.error("Unable to delete topic {}, {}", clusterAclRequest.getTopicName(), e.getMessage());
      if (e.getMessage().contains("This server does not host this topic")) {
        log.warn(
            "Topic: {} do not exist in {}",
            clusterAclRequest.getTopicName(),
            clusterAclRequest.getEnv());
        return e.getMessage();
      }
      throw e;
    }

    return ApiResultStatus.SUCCESS.value;
  }

  public ApiResponse createTopic(ClusterTopicRequest clusterTopicRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info(
        "createTopic {} {} {}",
        clusterTopicRequest.getEnv(),
        clusterTopicRequest.getProtocol(),
        clusterTopicRequest.getClusterName());

    String createTopicsUri =
        env.getProperty(
            clusterTopicRequest.getClusterName().toLowerCase()
                + ".klaw.clusters.counfluentcloud.listtopics.api");

    if (null == createTopicsUri) {
      log.error("Exception: {}", "createTopics api/uri for confluent cloud is not configured");
      throw new Exception("Create topics api/uri for confluent cloud is not configured");
    }

    if (!Strings.isNullOrEmpty(createTopicsUri)) {
      createTopicsUri = clusterTopicRequest.getEnv() + createTopicsUri;
      if (KafkaSupportedProtocol.SSL == clusterTopicRequest.getProtocol()) {
        createTopicsUri = HTTPS_PREFIX + createTopicsUri;
      }
    }

    TopicCreateRequest topicCreateReq = getTopicCreateObj(clusterTopicRequest);

    HttpHeaders headers =
        clusterApiUtils.createHeaders(
            clusterTopicRequest.getClusterName(), KafkaClustersType.KAFKA);
    HttpEntity<TopicCreateRequest> request = new HttpEntity<>(topicCreateReq, headers);

    try {
      restTemplate.postForEntity(createTopicsUri, request, String.class);
    } catch (Exception e) {
      log.error(
          "Unable to create topic {}, {}", clusterTopicRequest.getTopicName(), e.getMessage());
      if (e.getMessage().contains("already exists")) {
        log.warn(
            "Topic: {} already exists in {}",
            clusterTopicRequest.getTopicName(),
            clusterTopicRequest.getEnv());
        return ApiResponse.builder().result(e.getMessage()).build();
      }
      throw e;
    }

    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
  }

  public ApiResponse deleteTopic(ClusterTopicRequest clusterTopicRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info(
        "createTopic {} {} {}",
        clusterTopicRequest.getEnv(),
        clusterTopicRequest.getProtocol(),
        clusterTopicRequest.getClusterName());

    String deleteTopicsUri =
        env.getProperty(
            clusterTopicRequest.getClusterName().toLowerCase()
                + ".klaw.clusters.counfluentcloud.listtopics.api");

    if (null == deleteTopicsUri) {
      log.error("Exception: {}", "deleteTopics api/uri for confluent cloud is not configured");
      throw new Exception("Delete topics api/uri for confluent cloud is not configured");
    }

    if (!Strings.isNullOrEmpty(deleteTopicsUri)) {
      deleteTopicsUri =
          clusterTopicRequest.getEnv() + deleteTopicsUri + "/" + clusterTopicRequest.getTopicName();
      if (KafkaSupportedProtocol.SSL == clusterTopicRequest.getProtocol()) {
        deleteTopicsUri = HTTPS_PREFIX + deleteTopicsUri;
      }
    }

    HttpHeaders headers =
        clusterApiUtils.createHeaders(
            clusterTopicRequest.getClusterName(), KafkaClustersType.KAFKA);
    HttpEntity<String> request = new HttpEntity<>(headers);

    try {
      restTemplate.exchange(deleteTopicsUri, HttpMethod.DELETE, request, String.class);
    } catch (Exception e) {
      log.error(
          "Unable to delete topic {}, {}", clusterTopicRequest.getTopicName(), e.getMessage());
      if (e.getMessage().contains("This server does not host this topic")) {
        log.warn(
            "Topic: {} do not exist in {}",
            clusterTopicRequest.getTopicName(),
            clusterTopicRequest.getEnv());
        return ApiResponse.builder().result(e.getMessage()).build();
      }
      throw e;
    }

    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
  }

  private TopicCreateRequest getTopicCreateObj(ClusterTopicRequest clusterTopicRequest) {
    TopicCreateRequest topicObject = new TopicCreateRequest();
    topicObject.setTopic_name(clusterTopicRequest.getTopicName());
    topicObject.setPartitions_count(clusterTopicRequest.getPartitions());
    topicObject.setReplication_factor(clusterTopicRequest.getReplicationFactor());
    ArrayList<Config> configs = new ArrayList<>();
    if (clusterTopicRequest.getAdvancedTopicConfiguration() != null) {
      for (String key : clusterTopicRequest.getAdvancedTopicConfiguration().keySet()) {
        Config config = new Config();
        config.setName(key);
        config.setValue(clusterTopicRequest.getAdvancedTopicConfiguration().get(key));
        configs.add(config);
      }
    }
    topicObject.setConfigs(configs);

    return topicObject;
  }

  private static Map<String, String> updateAclMap(
      ClusterAclRequest clusterAclRequest, Map<String, String> aclMap) {
    if (clusterAclRequest.isPrefixAcl()) {
      aclMap.put("pattern_type", AclPatternType.PREFIXED.value);
    } else {
      aclMap.put("pattern_type", AclPatternType.LITERAL.value);
    }

    if (AclIPPrincipleType.PRINCIPAL.name().equals(clusterAclRequest.getAclIpPrincipleType())) {
      aclMap.put("principal", clusterAclRequest.getAclSsl());
      aclMap.put("host", "*");
    } else {
      aclMap.put("principal", "User:*");
      aclMap.put("host", clusterAclRequest.getAclIp());
    }

    if (clusterAclRequest.getRequestOperationType().equals(RequestOperationType.CREATE)) {
      aclMap.put("permission", "ALLOW");
    }
    return aclMap;
  }

  private List<Map<String, String>> processListTopicsResponse(
      ResponseEntity<ListTopicsResponse> responseEntity) {
    ListTopicsResponse topicsList = Objects.requireNonNull(responseEntity.getBody());
    List<Map<String, String>> topicsListUpdated = new ArrayList<>();
    for (TopicObject topicObject : topicsList.data) {
      Map<String, String> topicsMapUpdated = new HashMap<>();
      topicsMapUpdated.put("topicName", topicObject.topic_name);
      topicsMapUpdated.put("replicationFactor", "" + topicObject.replication_factor);
      topicsMapUpdated.put("partitions", "" + topicObject.partitions_count);

      topicsListUpdated.add(topicsMapUpdated);
    }
    return topicsListUpdated;
  }

  private static List<Map<String, String>> processListAclsResponse(
      ResponseEntity<ListAclsResponse> responseEntity) {
    ListAclsResponse aclsList = Objects.requireNonNull(responseEntity.getBody());
    List<Map<String, String>> aclsListUpdated = new ArrayList<>();
    for (ConfluentCloudAclObject confluentCloudAclObject : aclsList.data) {
      Map<String, String> aclsMapUpdated = new HashMap<>();
      aclsMapUpdated.put(
          "operation", confluentCloudAclObject.operation.toUpperCase()); // DESCRIBE/READ/ALL..
      aclsMapUpdated.put(
          "resourceType", confluentCloudAclObject.resource_type); // TOPIC/GROUP/CLUSTER..
      aclsMapUpdated.put(
          "resourceName", confluentCloudAclObject.resource_name); // topic-name, consumergroupname..
      aclsMapUpdated.put("principle", confluentCloudAclObject.principal); // User:*/username/ssldn..
      aclsMapUpdated.put("host", confluentCloudAclObject.host); // ipaddress/*..
      aclsMapUpdated.put("permissionType", confluentCloudAclObject.permission); // ALLOW/DENY..

      aclsListUpdated.add(aclsMapUpdated);
    }
    return aclsListUpdated;
  }

  private RestTemplate getRestTemplate() {
    return clusterApiUtils.getRequestDetails("", KafkaSupportedProtocol.SSL).getRight();
  }

  public ApiResponse updateTopic(ClusterTopicRequest clusterTopicRequest) {
    // TODO
    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
  }

  //  @Override
  //  public void afterPropertiesSet() throws Exception {
  //    getAclsTopics();
  //  }
}
