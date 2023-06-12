package io.aiven.klaw.clusterapi.models.connect;

import java.util.List;
import lombok.Data;

@Data
public class Status {
  public String name;
  public Connector connector;
  public List<Task> tasks;
  public String type;
}
