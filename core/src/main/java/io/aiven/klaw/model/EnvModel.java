package io.aiven.klaw.model;

import io.aiven.klaw.model.enums.KafkaClustersType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class EnvModel implements Serializable {

  private String id;

  @NotNull
  @Size(min = 2, max = 10, message = "Please fill in a valid name")
  @Pattern(message = "Invalid environment name !!", regexp = "^[a-zA-Z0-9_.-]{3,}$")
  private String name;

  @NotNull private String type;

  private Integer tenantId;

  @Pattern(message = "Invalid prefix", regexp = "^$|^[a-zA-Z0-9_.-]{3,}$")
  private String topicprefix;

  @Pattern(message = "Invalid suffix", regexp = "^$|^[a-zA-Z0-9_.-]{3,}$")
  private String topicsuffix;

  @NotNull private Integer clusterId;

  private String tenantName;

  private String clusterName;

  private String envStatus;

  private String otherParams;

  private String defaultPartitions;

  private String maxPartitions;

  private String defaultReplicationFactor;

  private String maxReplicationFactor;

  private boolean showDeleteEnv;

  private String totalNoPages;
  private List<String> allPageNos;

  private String associatedEnv;

  public void setClusterType(KafkaClustersType type) {
    this.type = type.value;
  }
}
