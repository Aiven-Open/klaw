package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnvUpdatedStatus {
  @NotNull private String result;

  @NotNull private String envstatus;
}
