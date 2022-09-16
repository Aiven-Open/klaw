package io.aiven.klaw.model;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class KwClustersModel implements Serializable {

  private Integer clusterId;

  @NotNull
  @Pattern(message = "Invalid cluster name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String clusterName;

  @NotNull
  @Size(min = 6, message = "Invalid bootstrap servers")
  @Pattern(message = "Invalid bootstrap servers", regexp = "^[a-zA-Z0-9._:,-]{3,}$")
  private String bootstrapServers;

  @NotNull(message = "Protocol cannot be null")
  private String protocol;

  @NotNull private String clusterType;

  @NotNull private String kafkaFlavor;

  private String projectName;

  private String serviceName;

  private String publicKey;

  private boolean showDeleteCluster;

  private String clusterStatus;

  private String totalNoPages;
  private List<String> allPageNos;
}
