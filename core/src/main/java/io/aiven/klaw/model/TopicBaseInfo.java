package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** All the Information from TopicConfig and the ownership & environment of a topic. */
@Data
public class TopicBaseInfo extends TopicConfig {

  @NotNull private String teamname;

  @NotNull private int teamId;

  @NotNull private String envId;

  @NotNull private String envName;

  @NotNull private boolean showEditTopic;

  @NotNull private boolean showDeleteTopic;

  @NotNull private boolean topicDeletable;
}
