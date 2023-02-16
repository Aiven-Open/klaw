package io.aiven.klaw.clusterapi.models.confluentcloud;

import lombok.Data;

/*
Configs are topic configs within each topic, returned by Confluent cloud in a list of topics
 */
@Data
public class Configs {
  public String related;
}
