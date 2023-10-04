package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

/**
 * All the information on a specific topic ownership & environment and inherited configuration of
 * the topic.
 */
@Data
public class TopicOverviewInfo extends TopicBaseInfo {

  @NotNull private boolean isTopicOwner;

  @NotNull private boolean isHighestEnv;

  @NotNull private boolean hasOpenRequest;

  @NotNull private boolean hasOpenTopicRequest;

  @NotNull private boolean hasOpenACLRequest;

  @NotNull private boolean hasOpenSchemaRequest;

  @NotNull private boolean hasOpenClaimRequest;

  @NotNull private boolean hasOpenRequestOnAnyEnv;

  @NotNull private boolean hasACL;

  @NotNull private boolean hasSchema;

  @NotNull private String envName;

  @NotNull private Integer clusterId;

  private Map<String, String> advancedTopicConfiguration;
}
