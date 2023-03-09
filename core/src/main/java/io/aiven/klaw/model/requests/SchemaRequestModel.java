package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemaRequestModel implements Serializable {

  private Integer req_no;

  @NotNull
  @Size(min = 8, message = "Please fill in a valid schema.")
  private String schemafull;

  private String remarks;

  private String topicname;

  private String environment;

  private String environmentName;

  private String schemaversion;

  private String teamname;

  private Integer teamId;

  private String appname;

  private String username;

  private Timestamp requesttime;

  private String requesttimestring;

  private RequestStatus requestStatus;

  private RequestOperationType requestOperationType;

  private Boolean forceRegister;

  private String approver;

  private Timestamp approvingtime;

  private String approvingTeamDetails;

  public Integer getReq_no() {
    return req_no;
  }

  public void setReq_no(Integer req_no) {
    this.req_no = req_no;
  }

  private String totalNoPages;

  private List<String> allPageNos;

  private String currentPage;

  private boolean isEditable;
  private boolean isDeletable;
}
