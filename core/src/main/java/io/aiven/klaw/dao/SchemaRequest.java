package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(SchemaRequestID.class)
@Table(name = "kwschemarequests")
public class SchemaRequest implements Serializable {

  @Id
  @Column(name = "avroschemaid")
  private Integer req_no;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Transient private String environmentName;

  @Column(name = "topicname")
  private String topicname;

  @Column(name = "env")
  private String environment;

  @Column(name = "versionschema")
  private String schemaversion;

  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "appname")
  private String appname;

  @Column(name = "schemafull")
  private String schemafull;

  @Column(name = "requestor")
  private String username;

  @Column(name = "requesttime")
  private Timestamp requesttime;

  @Transient private String requesttimestring;

  @Column(name = "topicstatus")
  private String topicstatus;

  @Column(name = "requesttype")
  private String requesttype;

  @Column(name = "remarks")
  private String remarks;

  @Column(name = "approver")
  private String approver;

  @Column(name = "exectime")
  private Timestamp approvingtime;
}
