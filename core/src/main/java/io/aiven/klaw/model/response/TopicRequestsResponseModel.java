package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicConfigEntry;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TopicRequestsResponseModel extends BaseRequestsResponseModel implements Serializable {

  @NotNull private String topicname;

  @NotNull private Integer topicpartitions;

  @NotNull private String replicationfactor;

  @NotNull private String description;

  @NotNull private Integer topicid;

  @NotNull private Boolean deleteAssociatedSchema;

  @NotNull private List<TopicConfigEntry> advancedTopicConfigEntries;

  private String approvingTeamId;

  private String sequence;

  private String otherParams;

  private List<String> possibleTeams;
}
