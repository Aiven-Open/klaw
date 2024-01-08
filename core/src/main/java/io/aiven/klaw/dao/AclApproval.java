package io.aiven.klaw.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "kwaclapprovals")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AclApproval extends Approval implements Serializable {

  @ManyToOne
  @JoinColumns({@JoinColumn(name = "req_no"), @JoinColumn(name = "tenantId")})
  @JsonIgnoreProperties("approvals")
  // Json Ignore here stops a recursive pull of data causing a stack overflow
  private AclRequests parent;

  public AclApproval(Approval approval) {
    super(approval);
  }

  public AclApproval() {
    super();
  }
}
