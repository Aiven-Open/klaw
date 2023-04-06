package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.KafkaSupportedProtocol;
import io.aiven.klaw.validation.KafkaClusterValidator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@KafkaClusterValidator
public class KwClustersModel implements Serializable {

  private Integer clusterId;

  @NotNull
  @Pattern(message = "Invalid cluster name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String clusterName;

  @NotNull
  @Size(min = 6, message = "Invalid bootstrap servers")
  @Pattern(message = "Invalid bootstrap servers", regexp = "^[a-zA-Z0-9._:,-/]{3,}$")
  private String bootstrapServers;

  @NotNull(message = "Protocol cannot be null")
  private KafkaSupportedProtocol protocol;

  @NotNull private String clusterType;

  @NotNull private String kafkaFlavor;

  private String associatedServers;

  private String projectName;

  private String serviceName;

  private String publicKey;
}
