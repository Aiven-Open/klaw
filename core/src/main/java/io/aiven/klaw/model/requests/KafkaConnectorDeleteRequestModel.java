package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KafkaConnectorDeleteRequestModel {

  @NotNull private String connectorName;
  @NotNull private String envId;
}
