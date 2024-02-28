package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemaPromotion {
  @NotNull
  @Pattern(message = "Invalid target environment Id. Pattern [0-9]", regexp = "^[0-9]*$")
  private String targetEnvironment;

  @NotNull
  @Pattern(message = "Invalid Team source environment Id. Pattern [0-9]", regexp = "^[0-9]*$")
  private String sourceEnvironment;

  @NotNull
  @Pattern(
      message = "Invalid Topic name. Pattern [a-zA-z0-9] (Spaces NOT allowed)",
      regexp = "^[a-zA-z 0-9]*$")
  private String topicName;

  @NotNull
  @Pattern(message = "Invalid Schema Version. Pattern [0-9]", regexp = "^[0-9]*$")
  private String schemaVersion;

  private boolean forceRegister;

  @NotNull
  @Pattern(
      message = "Invalid App name. Pattern [a-zA-z 0-9] (Spaces allowed)",
      regexp = "^[a-zA-z 0-9]*$")
  private String appName;

  @NotNull
  @Pattern(
      message = "Invalid Remarks. Pattern [a-zA-z. 0-9] (Spaces allowed)",
      regexp = "^[a-zA-z 0-9]*$")
  private String remarks;
}
