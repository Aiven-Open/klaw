package io.aiven.klaw.dao;

import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** To store requests like reset consumer offsets, flush topic reqs etc */
@ToString
@Getter
@Setter
@Entity
@IdClass(OperationalRequestID.class)
@Table(name = "kwoperationalrequests")
public class OperationalRequest implements Serializable {

  @Id
  @Column(name = "reqid")
  private Integer reqId;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "topicname")
  private String topicname;

  @Column(name = "consumerGroup")
  private String consumerGroup;

  @Column(name = "offsetresettype")
  @Enumerated(EnumType.STRING)
  private OffsetResetType offsetResetType;

  @Column(name = "resettimestamp")
  private Timestamp resetTimeStamp;

  @Column(name = "env")
  private String environment;

  @Column(name = "teamid")
  private Integer requestingTeamId;

  @Column(name = "appname")
  private String appname;

  @Column(name = "requestname")
  @Enumerated(EnumType.STRING)
  private OperationalRequestType operationalRequestType;

  @Column(name = "requestor")
  private String requestor;

  @Column(name = "requesttime")
  private Timestamp requesttime;

  @Column(name = "requeststatus")
  private String requestStatus;

  @Column(name = "remarks")
  private String remarks;

  @Column(name = "approver")
  private String approver;

  @Column(name = "exectime")
  private Timestamp approvingtime;

  @Column(name = "otherparams")
  private String otherParams;

  @Column(name = "approvingteamid")
  private String approvingTeamId;

  @Transient private String environmentName;

  @Transient private String requesttimestring;

  @Transient private String totalNoPages;

  @Transient private String currentPage;

  @Transient private List<String> allPageNos;
}
