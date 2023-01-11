package io.aiven.klaw.clusterapi.models;

import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.RequestOperationType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClusterAclRequest implements Serializable {

  @NotNull private String aclNativeType;

  @NotNull private RequestOperationType requestOperationType;

  @NotNull private String topicName;

  private String projectName;

  private String serviceName;

  private String username;

  private String permission;

  private String env;

  private KafkaSupportedProtocol protocol;

  private String clusterName;

  private String consumerGroup;

  private String aclType;

  private String aclIp;

  private String aclSsl;

  private String transactionalId;

  private String aclIpPrincipleType;

  private boolean isPrefixAcl;

  private String aivenAclKey;
}
