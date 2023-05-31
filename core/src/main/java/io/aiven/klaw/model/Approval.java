package io.aiven.klaw.model;

import io.aiven.klaw.model.enums.ApprovalType;
import lombok.Data;

@Data
public class Approval {

  private String approvalId;

  private ApprovalType approvalType;

  private String requiredApprovingTeamName;

  // Following three are filled in by the approver
  private String approverName;

  private int approverTeamId;

  private String approverTeamName;

  public Approval() {}

  public Approval(Approval approval) {
    this.setApproverName(approval.getApproverName());
    this.setApprovalId(approval.getApprovalId());
    this.setApprovalType(approval.getApprovalType());
    this.setApproverTeamName(approval.getApproverTeamName());
    this.setRequiredApprovingTeamName(approval.getRequiredApprovingTeamName());
    this.setApproverTeamId(approval.getApproverTeamId());
  }
}
