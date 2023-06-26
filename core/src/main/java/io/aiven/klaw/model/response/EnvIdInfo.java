package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnvIdInfo {
  @NotNull private String id;

  @NotNull private String name;
}
