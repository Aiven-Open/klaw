package io.aiven.klaw.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(ActivityLogID.class)
@Table(name = "kwactivitylog")
public class ActivityLog implements Serializable {

  @Id
  @Column(name = "kwreqno")
  private Integer req_no;

  @Id
  @Column(name = "tenantid")
  private int tenantId;

  @Column(name = "activityname")
  private String activityName;

  @Column(name = "activitytype")
  private String activityType;

  @Column(name = "activitytime")
  private Timestamp activityTime;

  @Transient private String activityTimeString;

  @Column(name = "details")
  private String details;

  @Column(name = "userid")
  private String user;

  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "env")
  private String env;

  @Transient private String envName;

  @Transient private String team;

  @Transient private String totalNoPages;

  @Transient private String currentPage;

  @Transient private List<String> allPageNos;
}
