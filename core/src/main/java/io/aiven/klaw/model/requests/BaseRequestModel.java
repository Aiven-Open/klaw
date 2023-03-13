package io.aiven.klaw.model.requests;

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
public class BaseRequestModel implements Serializable {

  @NotNull private String environment;

  private String environmentName;

  private String appname;

  private Integer teamId;

  private String teamname;

  // CREATE / DELETE / ..
  private RequestOperationType requestOperationType;

  private RequestStatus requestStatus;

  private String remarks;

  private Timestamp requesttime;

  private String requesttimestring;

  private String username;

  private String approver;

  private Timestamp approvingtime;

  private boolean isDeletable;

  private boolean isEditable;

  private String currentPage;

  private String totalNoPages;

  private List<String> allPageNos;

  private String approvingTeamDetails;
}
