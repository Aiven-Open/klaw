package io.aiven.klaw.model.cluster;

import lombok.Data;

@Data
public class ConnectorState {
  private String connectorName;
  private String connectorStatus;
  private long runningTasks;
  private long failedTasks;
}
