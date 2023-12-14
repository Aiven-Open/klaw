package io.aiven.klaw.dao;

import io.aiven.klaw.helpers.AclIPPrincipleTypeConverter;
import io.aiven.klaw.helpers.AivenAclIdConverter;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@IdClass(AclRequestID.class)
@Table(name = "kwaclrequests")
public class AclRequests implements Serializable {

  @Id
  @Column(name = "aclid")
  private Integer req_no;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "topicname")
  private String topicname;

  @Column(name = "env")
  private String environment;

  @Transient private String environmentName;

  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "requestingteam")
  private Integer requestingteam;

  @Column(name = "appname")
  private String appname;

  // Producer Consumer
  @Column(name = "topictype")
  private String aclType;

  @Column(name = "consumergroup")
  private String consumergroup;

  @Column(name = "requestor")
  private String requestor;

  @Column(name = "requesttime")
  private Timestamp requesttime;

  @Transient private String requesttimestring;

  @Column(name = "topicstatus")
  private String requestStatus;

  @Column(name = "remarks")
  private String remarks;

  @Column(name = "aclip")
  private String acl_ip;

  @Column(name = "aclssl")
  private String acl_ssl;

  @Column(name = "approver")
  private String approver;

  @Column(name = "exectime")
  private Timestamp approvingtime;

  // Create or Delete
  @Column(name = "acltype")
  private String requestOperationType;

  // prefixed acls or Literal(default)
  @Column(name = "aclpatterntype")
  private String aclPatternType;

  // Topic
  @Column(name = "aclresourcetype")
  private String aclResourceType;

  // TransactionalID
  @Column(name = "transactionalid")
  private String transactionalId;

  @Column(name = "otherparams")
  private String otherParams;

  @Column(name = "jsonparams")
  @Convert(converter = AivenAclIdConverter.class)
  private Map<String, String> jsonParams;

  @Column(name = "aclipprincipletype")
  @Convert(converter = AclIPPrincipleTypeConverter.class)
  private AclIPPrincipleType aclIpPrincipleType;

  @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<AclApproval> approvals = new ArrayList<>();

  @Column(name = "associatedaclid")
  private Integer associatedAclId;

  @Transient private String totalNoPages;

  @Transient private String currentPage;

  @Transient private List<String> allPageNos;
}
