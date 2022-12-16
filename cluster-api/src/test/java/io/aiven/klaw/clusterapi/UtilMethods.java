package io.aiven.klaw.clusterapi;

import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
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

  public Set<Map<String, String>> getTopics() {
    Set<Map<String, String>> topicsSet = new HashSet<>();
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("topicName", "testtopic1");

    hashMap.put("partitions", "2");
    hashMap.put("replicationFactor", "1");
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
    return ClusterTopicRequest.builder()
        .env("localhost")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .topicName("testtopic")
        .partitions(2)
        .replicationFactor(Short.parseShort("1"))
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
        .aclIpPrincipleType(AclIPPrincipleType.PRINCIPAL.name())
        .build();
  }
}
