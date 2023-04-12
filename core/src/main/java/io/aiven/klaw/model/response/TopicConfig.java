package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class TopicConfig {
  private String topicName;

  private String replicationFactor;

  private String partitions;
}
