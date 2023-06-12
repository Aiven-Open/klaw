package io.aiven.klaw.clusterapi.models.connect;

import lombok.Data;

@Data
public class ConnectorState {
  private String connectorName;
  private String connectorStatus;
  private long runningTasks;
  private long failedTasks;
}
