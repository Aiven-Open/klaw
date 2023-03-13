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

  @NotNull
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

  @NotNull
  @Min(value = 1, message = "TopicPartitions must be greater than zero")
  private Integer topicpartitions;

  @NotNull
  @Min(value = 1, message = "Replication factor must be greater than zero")
  private String replicationfactor;

  private List<TopicConfigEntry> advancedTopicConfigEntries;

  @NotNull
  @Pattern(message = "Invalid description", regexp = "^[a-zA-Z 0-9_.,-]{3,}$")
  private String description;

  private String approvingTeamId;

  private Integer topicid;

  private String requestor;

  private String sequence;

  private String otherParams;

  private List<String> possibleTeams;

  private Boolean deleteAssociatedSchema;
}
