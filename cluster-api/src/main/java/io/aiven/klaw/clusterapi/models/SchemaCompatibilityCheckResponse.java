package io.aiven.klaw.clusterapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SchemaCompatibilityCheckResponse {

  @JsonProperty("is_compatible")
  private boolean isCompatible;
}
