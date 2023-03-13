package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.aiven.klaw.model.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.AclsNativeType;
import java.io.Serializable;
import java.util.Map;
import lombok.Builder;

@Builder(toBuilder = true)
public class ClusterTopicRequest implements Serializable {

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
