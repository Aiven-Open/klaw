package io.aiven.klaw.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TopicRequestModel implements Serializable {

  @NotNull
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

  @NotNull private String environment;

  @NotNull
  @Min(value = 1, message = "TopicPartitions must be greater than zero")
  private Integer topicpartitions;

  private String teamname;

  @Pattern(message = "Invalid remarks", regexp = "^$|^[a-zA-Z 0-9_.,-]{3,}$")
  private String remarks;

  @NotNull
  @Pattern(message = "Invalid description", regexp = "^[a-zA-Z 0-9_.,-]{3,}$")
  private String description;

  @NotNull
  @Min(value = 1, message = "Replication factor must be greater than zero")
  private String replicationfactor;

  private String environmentName;

  private Integer topicid;

  private List<TopicConfigEntry> advancedTopicConfigEntries;

  private String appname;

  private String topictype;

  private String requestor;

  private Timestamp requesttime;

  private String requesttimestring;

  private String topicstatus;

  private String approver;

  private Timestamp approvingtime;

  private String sequence;

  private String username;

  private String totalNoPages;

  private String approvingTeamDetails;

  private String otherParams;

  private Integer teamId;

  private List<String> allPageNos;

  private List<String> possibleTeams;

  private String currentPage;

  private boolean isDeletable;

  private boolean isEditable;
}
