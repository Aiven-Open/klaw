package com.kafkamgt.uiapi.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KafkaConnectorRequestModel implements Serializable {

  @NotNull
  @Pattern(message = "Invalid connector name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String connectorName;

  private String connectorConfig;

  @NotNull private String environment;

  @NotNull private String teamName;

  @Pattern(message = "Invalid remarks", regexp = "^$|^[a-zA-Z 0-9_.,-]{3,}$")
  private String remarks;

  @NotNull
  @Pattern(message = "Invalid description", regexp = "^[a-zA-Z 0-9_.,-]{3,}$")
  private String description;

  private String environmentName;

  private Integer teamId;

  private Integer connectorId;

  private String connectortype;

  private String requestor;

  private Timestamp requesttime;

  private String requesttimestring;

  private String connectorStatus;

  private String approver;

  private Timestamp approvingtime;

  private String sequence;

  private String username;

  private String totalNoPages;

  private String approvingTeamDetails;

  private String otherParams;

  private List<String> allPageNos;

  private List<String> possibleTeams;

  private String currentPage;
}
