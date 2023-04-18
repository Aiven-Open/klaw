package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class ConnectivityStatus {
  private String clusterType;

  private String connectionStatus;
}
