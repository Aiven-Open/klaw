package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class ConnectorOverviewPerEnv {
  private boolean connectorExists;
  private String error;
  private Integer connectorId;
  private KafkaConnectorModelResponse connectorContents;
}
