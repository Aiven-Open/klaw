package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class TopicOverviewInfo {

  @NotNull private String topicName;

  @NotNull private Integer noOfPartitions;

  @NotNull private String noOfReplicas;

  @NotNull private String teamname;

  @NotNull private int teamId;

  @NotNull private String envId;

  @NotNull private boolean showEditTopic;

  @NotNull private boolean showDeleteTopic;

  @NotNull private boolean topicDeletable;

  @NotNull private boolean isTopicOwner;

  @NotNull private boolean isHighestEnv;

  @NotNull private boolean hasOpenRequest;

  @NotNull private boolean hasOpenTopicRequest;

  @NotNull private boolean hasOpenACLRequest;

  @NotNull private boolean hasACL;

  @NotNull private boolean hasSchema;

  @NotNull private String envName;

  private Map<String, String> advancedTopicConfiguration;
}
