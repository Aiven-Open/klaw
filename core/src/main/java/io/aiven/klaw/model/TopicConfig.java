package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

/** All the Information on the configuration of a single topic. */
@Data
public class TopicConfig {

  @NotNull private String topicName;

  @NotNull private Integer noOfPartitions;

  @NotNull private String noOfReplicas;

  @NotNull private String description;

  private Map<String, String> advancedTopicConfiguration;
}
