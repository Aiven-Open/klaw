package io.aiven.klaw.model;

import io.aiven.klaw.model.response.EnvIdInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class TopicInfo {
  @NotNull private Integer topicid;

  @NotNull private String sequence;

  @NotNull private String topicName;

  @NotNull private Integer noOfPartitions;

  @NotNull private String description;

  @NotNull private String noOfReplicas;

  @NotNull private String teamname;

  @NotNull private int teamId;

  @NotNull private String envId;

  @NotNull private List<EnvIdInfo> environmentsList;

  @NotNull private boolean showEditTopic;

  @NotNull private boolean showDeleteTopic;

  @NotNull private boolean topicDeletable;

  @NotNull private String totalNoPages;

  @NotNull private String currentPage;

  @NotNull private List<String> allPageNos;

  private String documentation;

  private String envName;
}
