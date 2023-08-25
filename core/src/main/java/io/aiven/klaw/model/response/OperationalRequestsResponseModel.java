package io.aiven.klaw.model.response;

import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OperationalRequestsResponseModel implements Serializable {

  @NotNull private String topicname;

  @NotNull private String consumerGroup;

  @NotNull private OffsetResetType offsetResetType;

  private String resetTimeStampStr;

  @NotNull private String description;

  @NotNull private Integer reqId;

  @NotNull private String environment;

  @NotNull private String environmentName;

  @NotNull private String requestor;

  // topic owner team id
  @NotNull private Integer teamId;

  // topic owner team
  @NotNull private String teamname;

  @NotNull private OperationalRequestType operationalRequestType;

  @NotNull private RequestStatus requestStatus;

  @NotNull private Timestamp requesttime;

  @NotNull private String requesttimestring;

  @NotNull private boolean isDeletable;

  @NotNull private boolean isEditable;

  @NotNull private String currentPage;

  @NotNull private String totalNoPages;

  @NotNull private List<String> allPageNos;

  @NotNull private String approvingTeamDetails;

  private String approver;

  private Timestamp approvingtime;

  private String remarks;

  private String appname;

  private String otherParams;

  private String approvingTeamId;

  private String sequence;

  private List<String> possibleTeams;
}
