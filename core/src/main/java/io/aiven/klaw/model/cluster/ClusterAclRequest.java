package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.io.Serializable;
import lombok.Builder;

@Builder(toBuilder = true)
public class ClusterAclRequest implements Serializable {

  @JsonProperty private String aclNativeType;

  @JsonProperty private String projectName;

  @JsonProperty private String serviceName;

  @JsonProperty private String username;

  @JsonProperty private String permission;

  @JsonProperty private String env;

  @JsonProperty private KafkaSupportedProtocol protocol;

  @JsonProperty private String clusterName;

  @JsonProperty private String topicName;

  @JsonProperty private String consumerGroup;

  @JsonProperty private String aclType;

  @JsonProperty private String aclIp;

  @JsonProperty private String aclSsl;

  @JsonProperty private String transactionalId;

  @JsonProperty private String aclIpPrincipleType;

  @JsonProperty private boolean isPrefixAcl;

  @JsonProperty private String aivenAclKey;

  @JsonProperty private RequestOperationType requestOperationType;
}
