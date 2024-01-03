package io.aiven.klaw.dao;

import io.aiven.klaw.model.enums.ApprovalType;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

@Data
@MappedSuperclass
public class Approval implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "approvalid", nullable = false)
  private Integer approvalId;

  @Column(name = "approvaltype")
  private ApprovalType approvalType;

  @Column(name = "requiredapprover")
  private String requiredApprover;

  // Following three are filled in by the approver
  @Column(name = "approvername")
  private String approverName;

  @Column(name = "approverteamid")
  private int approverTeamId;

  @Column(name = "approverteamname")
  private String approverTeamName;

  public Approval() {}

  public Approval(Approval approval) {
    this.setApprovalId(approval.getApprovalId());
    this.setApproverName(approval.getApproverName());
    this.setApprovalType(approval.getApprovalType());
    this.setApproverTeamName(approval.getApproverTeamName());
    this.setRequiredApprover(approval.getRequiredApprover());
    this.setApproverTeamId(approval.getApproverTeamId());
  }
}
