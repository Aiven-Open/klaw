package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.TopicConfigEntry;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TopicRequestModel extends BaseRequestModel implements Serializable {
  // Validation is overridden by TopicRequestValidatorImpl on TopicCreateRequests.
  @NotNull(message = "Topic name must not be null")
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

  @NotNull(message = "Topic partitions must not be null")
  @Min(value = 1, message = "Topic Partitions must be greater than zero")
  private Integer topicpartitions;

  @NotNull(message = "Topic replication must not be null")
  @Min(value = 1, message = "Replication factor must be greater than zero")
  private String replicationfactor;

  private String description;

  private List<TopicConfigEntry> advancedTopicConfigEntries;

  private Integer teamId;

  private String approvingTeamId;

  private Boolean deleteAssociatedSchema;

  private String otherParams;
}
