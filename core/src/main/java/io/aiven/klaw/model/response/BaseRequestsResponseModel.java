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

  // CREATE / DELETE / ..
  @NotNull private RequestOperationType requestOperationType;

  @NotNull private String environment;

  @NotNull private String environmentName;

  @NotNull private String requestor;

  // topic owner team
  @NotNull private Integer teamId;

  @NotNull private String teamname;

  @NotNull private RequestStatus requestStatus;

  @NotNull private Timestamp requesttime;

  @NotNull private String requesttimestring;

  @NotNull private boolean isDeletable;

  @NotNull private boolean isEditable;

  @NotNull private String currentPage;

  @NotNull private String totalNoPages;

  @NotNull private List<String> allPageNos;

  private String approver;

  private Timestamp approvingtime;

  private String remarks;

  private String approvingTeamDetails;

  private String appname;

  private String otherParams;
}
