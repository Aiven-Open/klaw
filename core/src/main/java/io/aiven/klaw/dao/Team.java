package io.aiven.klaw.dao;

import io.aiven.klaw.helpers.ServiceAccountsConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "kwteams")
@IdClass(TeamID.class)
public class Team implements Serializable {

  @Id
  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "team")
  private String teamname;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "teammail")
  private String teammail;

  @Column(name = "app")
  private String app;

  @Column(name = "teamphone")
  private String teamphone;

  @Column(name = "contactperson")
  private String contactperson;

  @Column(name = "requesttopicsenvs")
  private String requestTopicsEnvs;

  @Column(name = "restrictionsobj")
  private String restrictionsObj;

  @Convert(converter = ServiceAccountsConverter.class)
  @Column(name = "serviceaccounts")
  private ServiceAccounts serviceAccounts;

  @Column(name = "otherparams")
  private String otherParams;

  @Column(name = "active")
  private boolean active;
}
