package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * All the information on a specific topic ownership & environment and inherited configuration of
 * the topic.
 */
@Data
public class TopicOverviewInfo extends TopicBaseInfo {

  @NotNull private boolean hasOpenRequest;

  @NotNull private boolean isTopicOwner;

  @NotNull private boolean isHighestEnv;

  @NotNull private boolean hasOpenACLRequest;
}
