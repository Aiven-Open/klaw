package io.aiven.klaw.model.response;

import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class EnvModelResponse implements Serializable {

  @NotNull private String id;

  @NotNull private String name;

  @NotNull private String type;

  @NotNull private Integer tenantId;

  @NotNull private Integer clusterId;

  @NotNull private String tenantName;

  @NotNull private String clusterName;

  @NotNull private ClusterStatus envStatus;

  @NotNull private String otherParams;

  @NotNull private boolean showDeleteEnv;

  @NotNull private String totalNoPages;

  @NotNull private String currentPage;

  @NotNull private List<String> allPageNos;

  @NotNull private int totalRecs;

  private EnvTag associatedEnv;

  @NotNull private EnvParams params;

  public void setClusterType(KafkaClustersType type) {
    this.type = type.value;
  }
}
