package io.aiven.klaw.model;

import lombok.Data;

@Data
public class SyncConnectorUpdates {
  private String sequence;
  // the unique identifier of the connector.
  private String connectorId;
  private String req_no;
  private String connectorName;
  private String teamSelected;
  private String envSelected;
}
