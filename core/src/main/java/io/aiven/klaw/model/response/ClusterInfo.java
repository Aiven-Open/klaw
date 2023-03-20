package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClusterInfo {
  @NotNull private boolean aivenCluster;
}
