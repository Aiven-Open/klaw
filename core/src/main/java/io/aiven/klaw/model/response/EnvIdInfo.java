package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnvIdInfo {
  @NotNull private String id;

  @NotNull private String name;
}
