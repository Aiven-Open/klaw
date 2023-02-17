package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@ToString
@Getter
@Setter
@Entity
@IdClass(KafkaConnectorRequestID.class)
@Table(name = "kwkafkaconnectorrequests")
public class KafkaConnectorRequest implements Serializable {

  @Id
  @Column(name = "connectorid")
  private Integer connectorId;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "connectorname")
  private String connectorName;

  @Column(name = "env")
  private String environment;

  @Column(name = "teamid")
  private Integer teamId;

  // Create, Update
  @Column(name = "connectortype")
  private String requestOperationType;

  @Column(name = "requestor")
  private String requestor;

  @Column(name = "requesttime")
  private Timestamp requesttime;

  @Column(name = "connectorstatus")
  private String requestStatus;

  @Column(name = "connectorconfig")
  private String connectorConfig;

  @Column(name = "remarks")
  private String remarks;

  @Column(name = "approver")
  private String approver;

  @Column(name = "exectime")
  private Timestamp approvingtime;

  @Column(name = "otherparams")
  private String otherParams;

  @Column(name = "description")
  private String description;

  @Transient private String requesttimestring;

  @Transient private String environmentName;

  @Transient private String sequence;

  @Transient private String username;

  @Transient private String totalNoPages;

  @Transient private String currentPage;

  @Transient private List<String> allPageNos;

  @Transient private List<String> possibleTeams;

  @Transient private String history;
}
