package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

/** All the Information on the configuration and ownership of a single topic. */
@Data
public class TopicBaseInfo {

  @NotNull private String topicName;

  @NotNull private Integer noOfPartitions;

  @NotNull private String noOfReplicas;

  @NotNull private String teamname;

  @NotNull private int teamId;

  @NotNull private String description;

  @NotNull private boolean showEditTopic;

  @NotNull private boolean showDeleteTopic;

  @NotNull private boolean topicDeletable;

  private Map<String, String> advancedTopicConfiguration;
}
