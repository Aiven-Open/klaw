package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class ConnectorPromotionStatus extends BasePromotionStatus {

  private String connectorName;
  private String sourceConnectorConfig;
}
