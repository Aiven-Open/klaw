package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "kwregisterusers")
public class RegisterUserInfo implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private int id;

  @Column(name = "userid")
  private String username;

  @Column(name = "pwd")
  private String pwd;

  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "roleid")
  private String role;

  @Column(name = "fullname")
  private String fullname;

  @Column(name = "mailid")
  private String mailid;

  @Column(name = "status")
  private String status;

  @Column(name = "registeredtime")
  private Timestamp registeredTime;

  @Column(name = "approver")
  private String approver;

  @Column(name = "registrationid")
  private String registrationId;

  @Column(name = "tenantid")
  private int tenantId;

  @Transient private String team;
}
