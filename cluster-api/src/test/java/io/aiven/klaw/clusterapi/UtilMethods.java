package io.aiven.klaw.clusterapi;

import io.aiven.klaw.clusterapi.models.AivenAclResponse;
import io.aiven.klaw.clusterapi.models.AivenAclStruct;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.TopicConfig;
import io.aiven.klaw.clusterapi.models.confluentcloud.AclObject;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListAclsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListTopicsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.TopicObject;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.RequestOperationType;
import java.util.*;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class UtilMethods {
  public List<AclBinding> getListAclBindings(AccessControlEntry accessControlEntry) {
    List<AclBinding> listAclBinding = new ArrayList<>();
    AclBinding aclBinding =
        new AclBinding(
            new ResourcePattern(ResourceType.GROUP, "consgroup1", PatternType.LITERAL),
            accessControlEntry);
    listAclBinding.add(aclBinding);

    return listAclBinding;
  }

  public Set<Map<String, String>> getAcls() {
    Set<Map<String, String>> aclsSet = new HashSet<>();
    Map<String, String> hMap = new HashMap<>();
    hMap.put("host", "12.11.124.11");
    hMap.put("principle", "User:*");
    hMap.put("operation", "READ");
    hMap.put("permissionType", "ALLOW");
    hMap.put("resourceType", "GROUP");
    hMap.put("resourceName", "consumergroup1");

    aclsSet.add(hMap);

    hMap = new HashMap<>();
    hMap.put("host", "12.15.124.12");
    hMap.put("principle", "User:*");
    hMap.put("operation", "READ");
    hMap.put("permissionType", "ALLOW");
    hMap.put("resourceType", "TOPIC");
    hMap.put("resourceName", "testtopic");
    aclsSet.add(hMap);

    return aclsSet;
  }

  public Set<TopicConfig> getTopics() {
    Set<TopicConfig> topicsSet = new HashSet<>();
    TopicConfig hashMap = new TopicConfig();
    hashMap.setTopicName("testtopic1");

    hashMap.setPartitions("2");
    hashMap.setReplicationFactor("1");
    topicsSet.add(hashMap);
    return topicsSet;
  }

  public MultiValueMap<String, String> getMappedValuesTopic() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("env", "localhost");
    params.add("protocol", "PLAINTEXT");
    params.add("topicName", "testtopic");
    params.add("partitions", "2");
    params.add("rf", "1");

    return params;
  }

  public ClusterTopicRequest getTopicRequest() {
    Map<String, String> advancedConfig = new HashMap<>();
    advancedConfig.put("compression.type", "snappy");
    advancedConfig.put("cleanup.policy", "delete");
    return ClusterTopicRequest.builder()
        .env("localhost")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .topicName("testtopic")
        .partitions(2)
        .replicationFactor(Short.parseShort("1"))
        .clusterName("DEV_CC1")
        .advancedTopicConfiguration(advancedConfig)
        .build();
  }

  public ClusterTopicRequest getConfluentCloudTopicRequest() {
    Map<String, String> advancedConfig = new HashMap<>();
    advancedConfig.put("compression.type", "snappy");
    advancedConfig.put("cleanup.policy", "delete");
    return ClusterTopicRequest.builder()
        .env("localhost")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .topicName("testtopic")
        .partitions(2)
        .replicationFactor(Short.parseShort("1"))
        .clusterName("DEV_CC1")
        .advancedTopicConfiguration(advancedConfig)
        .aclsNativeType(AclsNativeType.CONFLUENT_CLOUD)
        .build();
  }

  public MultiValueMap<String, String> getMappedValuesAcls(String aclType) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("env", "localhost");
    params.add("protocol", "PLAINTEXT");
    params.add("topicName", "testtopic");
    params.add("consumerGroup", "congroup1");
    params.add("aclType", aclType);
    params.add("acl_ip", "11.12.33.122");
    params.add("acl_ssl", null);

    return params;
  }

  public MultiValueMap<String, String> getMappedValuesSchema() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("env", "localhost");
    params.add("protocol", "PLAINTEXT");
    params.add("topicName", "testtopic");
    params.add("fullSchema", "{type:string}");

    return params;
  }

  public ClusterSchemaRequest getSchema() {
    return ClusterSchemaRequest.builder()
        .env("localhost")
        .fullSchema("{type:string}")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .topicName("testtopic")
        .clusterIdentification("CLID1")
        .build();
  }

  public ClusterAclRequest getAclRequest(String aclType) {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .consumerGroup("congroup1")
        .clusterName("clusterName")
        .aclType(aclType)
        .aclIp("11.12.33.122")
        .aclSsl(null)
        .requestOperationType(RequestOperationType.CREATE)
        .aclNativeType(AclsNativeType.NATIVE.name())
        .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
        .build();
  }

  public ClusterAclRequest getConfluentCloudProducerAclRequest() {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .consumerGroup("congroup1")
        .clusterName("clusterName")
        .aclType(AclType.PRODUCER.value)
        .aclIp("11.12.33.122")
        .aclSsl(null)
        .requestOperationType(RequestOperationType.CREATE)
        .aclNativeType(AclsNativeType.CONFLUENT_CLOUD.name())
        .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
        .build();
  }

  public ClusterAclRequest getConfluentCloudProducerPrefixedAclRequest() {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .consumerGroup("congroup1")
        .clusterName("clusterName")
        .aclType(AclType.PRODUCER.value)
        .aclIp("11.12.33.122")
        .aclSsl(null)
        .requestOperationType(RequestOperationType.CREATE)
        .aclNativeType(AclsNativeType.CONFLUENT_CLOUD.name())
        .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
        .isPrefixAcl(true)
        .build();
  }

  public ClusterAclRequest getConfluentCloudConsumerAclRequest() {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .consumerGroup("congroup1")
        .clusterName("clusterName")
        .aclType(AclType.CONSUMER.value)
        .aclIp(null)
        .aclSsl("CN=host")
        .requestOperationType(RequestOperationType.CREATE)
        .aclNativeType(AclsNativeType.CONFLUENT_CLOUD.name())
        .aclIpPrincipleType(AclIPPrincipleType.PRINCIPAL.name())
        .build();
  }

  public ClusterAclRequest getAivenAclRequest(String aclType) {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .clusterName("clusterName")
        .requestOperationType(RequestOperationType.CREATE)
        .aclNativeType(AclsNativeType.AIVEN.name())
        .aclIpPrincipleType(AclIPPrincipleType.PRINCIPAL.name())
        .projectName("testproject")
        .serviceName("testservice")
        .username("testuser")
        .permission("write")
        .build();
  }

  public AivenAclResponse getAivenAclResponse() {
    AivenAclResponse aivenAclResponse = new AivenAclResponse();
    AivenAclStruct aivenAclStruct = new AivenAclStruct();
    AivenAclStruct[] aivenAclStructs = new AivenAclStruct[1];
    aivenAclStruct.setId("testid");
    aivenAclStruct.setTopic("testtopic");
    aivenAclStruct.setPermission("write");
    aivenAclStruct.setUsername("testuser");
    aivenAclStructs[0] = aivenAclStruct;
    aivenAclResponse.setAcl(aivenAclStructs);
    aivenAclResponse.setMessage("success");

    return aivenAclResponse;
  }

  public ListTopicsResponse getConfluentCloudListTopicsResponse() {
    ListTopicsResponse listTopicsResponse = new ListTopicsResponse();
    TopicObject topicObject1 = new TopicObject();
    topicObject1.setTopic_name("testtopic1");
    topicObject1.setPartitions_count(2);
    topicObject1.setReplication_factor(2);

    TopicObject topicObject2 = new TopicObject();
    topicObject2.setTopic_name("testtopic2");
    topicObject2.setPartitions_count(4);
    topicObject2.setReplication_factor(3);

    ArrayList<TopicObject> topicObjectArrayList = new ArrayList<>();
    topicObjectArrayList.add(topicObject1);
    topicObjectArrayList.add(topicObject2);

    listTopicsResponse.setData(topicObjectArrayList);
    return listTopicsResponse;
  }

  public ListAclsResponse getConfluentCloudListAclsResponse() {
    ListAclsResponse listAclsResponse = new ListAclsResponse();
    AclObject aclObject1 = new AclObject();
    aclObject1.setPermission("ALLOW");
    aclObject1.setHost("12.12.43.123");
    aclObject1.setOperation("WRITE");
    aclObject1.setPrincipal("User:*");
    aclObject1.setPattern_type("LITERAL");
    aclObject1.setResource_type("TOPIC");
    aclObject1.setResource_name("testtopic");

    AclObject aclObject2 = new AclObject();
    aclObject2.setPermission("ALLOW");
    aclObject2.setHost("12.12.43.123");
    aclObject2.setOperation("DESCRIBE");
    aclObject2.setPrincipal("User:*");
    aclObject2.setPattern_type("LITERAL");
    aclObject2.setResource_type("TOPIC");
    aclObject2.setResource_name("testtopic");

    ArrayList<AclObject> aclObjectArrayList = new ArrayList<>();
    aclObjectArrayList.add(aclObject1);
    aclObjectArrayList.add(aclObject2);

    listAclsResponse.setData(aclObjectArrayList);

    return listAclsResponse;
  }
}
