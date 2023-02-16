package io.aiven.klaw.clusterapi.services;

import com.google.common.base.Strings;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.confluentcloud.AclObject;
import io.aiven.klaw.clusterapi.models.confluentcloud.Config;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListAclsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListTopicsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.TopicCreateRequest;
import io.aiven.klaw.clusterapi.models.confluentcloud.TopicObject;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.enums.AclPatternType;
import io.aiven.klaw.clusterapi.models.enums.AclType;
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
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/*
Confluent cloud API Reference
https://docs.confluent.io/cloud/current/api.html#tag/Topic-(v3)
https://docs.confluent.io/cloud/current/api.html#tag/ACL-(v3)
 */
@Service
@Slf4j
public class ConfluentCloudApiService {

  public static final String HTTPS_PREFIX = "https://";
  public static final String TOPIC_API_URI_KEY = ".klaw.clusters.counfluentcloud.topics.api";
  public static final String ACLS_API_URI_KEY = ".klaw.clusters.counfluentcloud.acls.api";
  private final Environment env;
  final ClusterApiUtils clusterApiUtils;

  public ConfluentCloudApiService(Environment env, ClusterApiUtils clusterApiUtils) {
    this.env = env;
    this.clusterApiUtils = clusterApiUtils;
  }

  public Set<Map<String, String>> listTopics(
      String restApiHost, KafkaSupportedProtocol protocol, String clusterIdentification)
      throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("loadTopics {} {} {}", restApiHost, protocol, clusterIdentification);

    String listTopicsUri =
        getResourceUri(
            clusterIdentification, restApiHost, "createTopic", ResourceType.TOPIC.name());

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

    String listAclsUri = getResourceUri(clusterIdentification, restApiHost, "listAclsUri", "ACLS");

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
        getResourceUri(
            clusterAclRequest.getClusterName(), clusterAclRequest.getEnv(), "createAcls", "ACLS");

    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterAclRequest.getClusterName(), KafkaClustersType.KAFKA);

    if (AclType.PRODUCER.value.equals(clusterAclRequest.getAclType())) {
      // Write on Topic
      applyOperation(
          clusterAclRequest,
          restTemplate,
          createAclsUri,
          headers,
          AclOperation.WRITE.name(),
          ResourceType.TOPIC.name(),
          clusterAclRequest.getTopicName());
      // Describe on Topic
      applyOperation(
          clusterAclRequest,
          restTemplate,
          createAclsUri,
          headers,
          AclOperation.DESCRIBE.name(),
          ResourceType.TOPIC.name(),
          clusterAclRequest.getTopicName());
      // Txn id access
      if (clusterAclRequest.getTransactionalId() != null
          && clusterAclRequest.getTransactionalId().length() > 0) {
        applyOperation(
            clusterAclRequest,
            restTemplate,
            createAclsUri,
            headers,
            AclOperation.WRITE.name(),
            ResourceType.TRANSACTIONAL_ID.name(),
            clusterAclRequest.getTransactionalId());
      }
    } else {
      // Read on Group, Describe on Group, Read on Topic
      applyOperation(
          clusterAclRequest,
          restTemplate,
          createAclsUri,
          headers,
          AclOperation.READ.name(),
          ResourceType.GROUP.name(),
          clusterAclRequest.getConsumerGroup());
      applyOperation(
          clusterAclRequest,
          restTemplate,
          createAclsUri,
          headers,
          AclOperation.DESCRIBE.name(),
          ResourceType.GROUP.name(),
          clusterAclRequest.getConsumerGroup());
      applyOperation(
          clusterAclRequest,
          restTemplate,
          createAclsUri,
          headers,
          AclOperation.READ.name(),
          ResourceType.TOPIC.name(),
          clusterAclRequest.getTopicName());
    }
    resultMap.put("result", ApiResultStatus.SUCCESS.value);
    return resultMap;
  }

  public String deleteAcls(ClusterAclRequest clusterAclRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("deleteAcls {}", clusterAclRequest);

    String baseAclsUri =
        getResourceUri(
            clusterAclRequest.getClusterName(), clusterAclRequest.getEnv(), "deleteAcls", "ACLS");

    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterAclRequest.getClusterName(), KafkaClustersType.KAFKA);
    HttpEntity<String> request = new HttpEntity<>(headers);

    if (clusterAclRequest.getAclType().equals(AclType.PRODUCER.value)) {
      // delete WRITE on Topic acls
      String deleteAclsUri =
          updateQueryParams(
              clusterAclRequest,
              ResourceType.TOPIC.name(),
              clusterAclRequest.getTopicName(),
              AclOperation.WRITE.name(),
              baseAclsUri);
      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);

      // delete DESCRIBE on Topic acls
      deleteAclsUri =
          updateQueryParams(
              clusterAclRequest,
              ResourceType.TOPIC.name(),
              clusterAclRequest.getTopicName(),
              AclOperation.DESCRIBE.name(),
              baseAclsUri);
      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);
    } else {
      // delete consumer group read acls
      String deleteAclsUri =
          updateQueryParams(
              clusterAclRequest,
              ResourceType.GROUP.name(),
              clusterAclRequest.getConsumerGroup(),
              AclOperation.READ.name(),
              baseAclsUri);
      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);

      // delete consumer group DESCRIBE acls
      deleteAclsUri =
          updateQueryParams(
              clusterAclRequest,
              ResourceType.GROUP.name(),
              clusterAclRequest.getConsumerGroup(),
              AclOperation.DESCRIBE.name(),
              baseAclsUri);
      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);

      // delete read topic acls
      deleteAclsUri =
          updateQueryParams(
              clusterAclRequest,
              ResourceType.TOPIC.name(),
              clusterAclRequest.getTopicName(),
              AclOperation.READ.name(),
              baseAclsUri);
      deleteAclsRestCall(restTemplate, deleteAclsUri, clusterAclRequest, request);
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
        getResourceUri(
            clusterTopicRequest.getClusterName(),
            clusterTopicRequest.getEnv(),
            "createTopic",
            ResourceType.TOPIC.name());

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
    log.info("createTopic {}", clusterTopicRequest);

    String deleteTopicsUri =
        getResourceUri(
            clusterTopicRequest.getClusterName(),
            clusterTopicRequest.getEnv(),
            "deleteTopic",
            ResourceType.TOPIC.name());
    deleteTopicsUri = deleteTopicsUri + "/" + clusterTopicRequest.getTopicName();

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

  // Confluent cloud doesn't provide api to update partitions/config of a topic. so either delete
  // the topic and recreate. Or this functionality is not provided by klaw.
  public ApiResponse updateTopic(ClusterTopicRequest clusterTopicRequest) throws Exception {
    //    deleteTopic(clusterTopicRequest);
    //    createTopic(clusterTopicRequest);
    return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
  }

  String updateQueryParams(
      ClusterAclRequest clusterAclRequest,
      String resourceType,
      String resourceName,
      String operation,
      String baseAclsUri) {
    DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseAclsUri);
    uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
    return uriBuilderFactory
        .builder()
        .queryParam("resource_type", resourceType)
        .queryParam("resource_name", resourceName)
        .queryParam(
            "pattern_type",
            (clusterAclRequest.isPrefixAcl() ? AclPatternType.PREFIXED : AclPatternType.LITERAL))
        .queryParam(
            "principal",
            (clusterAclRequest.getAclIpPrincipleType().equals(AclIPPrincipleType.PRINCIPAL.name())
                ? "User:" + clusterAclRequest.getAclSsl()
                : "User:*"))
        .queryParam(
            "host",
            (clusterAclRequest.getAclIpPrincipleType().equals(AclIPPrincipleType.IP_ADDRESS.name())
                ? clusterAclRequest.getAclIp()
                : "*"))
        .queryParam("operation", operation)
        .queryParam("permission", AclPermissionType.ALLOW.name())
        .build()
        .toString();
  }

  private String getResourceUri(
      String clusterIdentification, String host, String requestMethod, String requestType)
      throws Exception {
    String relevantUri;
    if (ResourceType.TOPIC.name().equals(requestType)) {
      relevantUri = env.getProperty(clusterIdentification.toLowerCase() + TOPIC_API_URI_KEY);
    } else {
      relevantUri = env.getProperty(clusterIdentification.toLowerCase() + ACLS_API_URI_KEY);
    }

    if (Strings.isNullOrEmpty(relevantUri)) {
      log.error("Exception: {}", requestMethod + " api/uri for confluent cloud is not configured");
      throw new Exception(requestMethod + " api/uri for confluent cloud is not configured");
    }
    relevantUri = HTTPS_PREFIX + host + relevantUri;

    return relevantUri;
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
        log.warn("Acl: {} does not exist", clusterAclRequest);
        return e.getMessage();
      }
      throw e;
    }

    return ApiResultStatus.SUCCESS.value;
  }

  private void applyOperation(
      ClusterAclRequest clusterAclRequest,
      RestTemplate restTemplate,
      String createAclsUri,
      HttpHeaders headers,
      String operation,
      String resourceType,
      String resourceName) {
    Map<String, String> aclMap;
    aclMap = new HashMap<>();
    aclMap.put("operation", operation);
    aclMap.put("resource_type", resourceType);
    aclMap.put("resource_name", resourceName);

    updateAclMap(clusterAclRequest, aclMap);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(aclMap, headers);

    // WRITE operation
    createAclsPostEntity(restTemplate, createAclsUri, request);
  }

  private void createAclsPostEntity(
      RestTemplate restTemplate, String createAclsUri, HttpEntity<Map<String, String>> request) {
    try {
      restTemplate.postForEntity(createAclsUri, request, String.class);
    } catch (Exception e) {
      log.error("Exception in creating acls : ", e);
      throw e;
    }
  }

  TopicCreateRequest getTopicCreateObj(ClusterTopicRequest clusterTopicRequest) {
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

  Map<String, String> updateAclMap(
      ClusterAclRequest clusterAclRequest, Map<String, String> aclMap) {
    if (clusterAclRequest.isPrefixAcl()) {
      aclMap.put("pattern_type", AclPatternType.PREFIXED.value);
    } else {
      aclMap.put("pattern_type", AclPatternType.LITERAL.value);
    }

    if (AclIPPrincipleType.PRINCIPAL.name().equals(clusterAclRequest.getAclIpPrincipleType())) {
      aclMap.put("principal", "User:" + clusterAclRequest.getAclSsl());
      aclMap.put("host", "*");
    } else {
      aclMap.put("principal", "User:*");
      aclMap.put("host", clusterAclRequest.getAclIp());
    }

    if (clusterAclRequest.getRequestOperationType().equals(RequestOperationType.CREATE)) {
      aclMap.put("permission", AclPermissionType.ALLOW.name());
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

  private List<Map<String, String>> processListAclsResponse(
      ResponseEntity<ListAclsResponse> responseEntity) {
    ListAclsResponse aclsList = Objects.requireNonNull(responseEntity.getBody());
    List<Map<String, String>> aclsListUpdated = new ArrayList<>();
    for (AclObject aclObject : aclsList.data) {
      Map<String, String> aclsMapUpdated = new HashMap<>();
      aclsMapUpdated.put("operation", aclObject.operation.toUpperCase()); // DESCRIBE/READ/ALL..
      aclsMapUpdated.put("resourceType", aclObject.resource_type); // TOPIC/GROUP/CLUSTER..
      aclsMapUpdated.put(
          "resourceName", aclObject.resource_name); // topic-name, consumergroupname..
      aclsMapUpdated.put("principle", aclObject.principal); // User:*/username/ssldn..
      aclsMapUpdated.put("host", aclObject.host); // ipaddress/*..
      aclsMapUpdated.put("permissionType", aclObject.permission); // ALLOW/DENY..

      aclsListUpdated.add(aclsMapUpdated);
    }
    return aclsListUpdated;
  }

  private RestTemplate getRestTemplate() {
    return clusterApiUtils.getRequestDetails("", KafkaSupportedProtocol.SSL).getRight();
  }
}
