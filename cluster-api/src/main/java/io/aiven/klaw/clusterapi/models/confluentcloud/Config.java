package io.aiven.klaw.clusterapi.models.confluentcloud;

import lombok.Data;

/*
Topic advanced configuration required to create a topic. Ex : compression.type:snappy
 */
@Data
public class Config {
  public String name;
  public String value;
}
