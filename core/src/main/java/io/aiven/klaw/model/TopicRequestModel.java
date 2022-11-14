package io.aiven.klaw.model;

import io.aiven.klaw.validation.TopicRequestValidator;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@TopicRequestValidator(getPermissionType = PermissionType.REQUEST_CREATE_TOPICS)
public class TopicRequestModel implements Serializable {

  @NotNull
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

  @NotNull private String environment;

  @NotNull
  @Min(value = 1, message = "TopicPartitions must be greater than zero")
  private Integer topicpartitions;

  @NotNull private String teamname;

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

  private List<String> advancedTopicConfigs;

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
}
