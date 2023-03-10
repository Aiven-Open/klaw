package io.aiven.klaw.clusterapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClusterTopicRequest {
  @JsonProperty private String env;

  @JsonProperty private int partitions;

  @JsonProperty private short replicationFactor;

  @JsonProperty private KafkaSupportedProtocol protocol;

  @JsonProperty private String clusterName;

  @JsonProperty private String topicName;

  @JsonProperty private Map<String, String> advancedTopicConfiguration;

  @JsonProperty private AclsNativeType aclsNativeType;

  @JsonProperty private Boolean deleteAssociatedSchema;

  // required to delete associated schema
  @JsonProperty private String schemaEnv;

  // required to delete associated schema
  @JsonProperty private String schemaClusterIdentification;

  // required to delete associated schema
  @JsonProperty private KafkaSupportedProtocol schemaEnvProtocol;
}
