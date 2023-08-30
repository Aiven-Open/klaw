package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectorClaimRequestModel {
  @NotNull private String connectorName;

  @NotNull private String env;
}
