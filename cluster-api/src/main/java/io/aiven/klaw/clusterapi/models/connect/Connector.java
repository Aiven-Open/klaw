package io.aiven.klaw.clusterapi.models.connect;

import lombok.Data;

@Data
public class Connector {
  public String state;
  public String worker_id;
}
