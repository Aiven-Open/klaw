package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@Table(name = "kwtenants")
public class KwTenants implements Serializable {

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "tenantname")
  private String tenantName;

  @Column(name = "tenantdesc")
  private String tenantDesc;

  @Column(name = "licenseexpiry")
  private Timestamp licenseExpiry;

  @Column(name = "contactperson")
  private String contactPerson;

  @Column(name = "intrial")
  private String inTrial;

  @Column(name = "isactive")
  private String isActive;

  @Column(name = "orgname")
  private String orgName;
}
