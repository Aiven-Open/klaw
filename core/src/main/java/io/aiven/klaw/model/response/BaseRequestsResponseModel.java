package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.RequestOperationType;
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
public class BaseRequestsResponseModel implements Serializable {

  @NotNull private String environment;

  @NotNull private String environmentName;

  @NotNull private String requestor;

  // topic owner team id
  @NotNull private Integer teamId;

  // topic owner team
  @NotNull private String teamname;

  // CREATE / DELETE / ..
  @NotNull private RequestOperationType requestOperationType;

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
}
