package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.ClusterStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EnvUpdatedStatus {
  @NotNull private String result;

  @NotNull private ClusterStatus envStatus;

  @NotNull private LocalDateTime lastUpdateTime;
}
