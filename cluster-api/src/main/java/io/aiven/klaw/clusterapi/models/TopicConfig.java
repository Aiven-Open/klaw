package io.aiven.klaw.clusterapi.models;

import lombok.Data;

@Data
public class TopicConfig {
  private String topicName;

  private String replicationFactor;

  private String partitions;
}
