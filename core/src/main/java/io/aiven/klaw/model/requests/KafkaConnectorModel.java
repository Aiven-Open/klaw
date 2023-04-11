package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KafkaConnectorModel {

  @NotNull private Integer connectorId;

  @NotNull private String connectorName;

  @NotNull private String documentation;
}
