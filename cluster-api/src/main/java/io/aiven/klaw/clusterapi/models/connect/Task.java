package io.aiven.klaw.clusterapi.models.connect;

import lombok.Data;

@Data
public class Task {
  public String connector;
  public int task;
  public int id;
  public String state;
  public String worker_id;
}
