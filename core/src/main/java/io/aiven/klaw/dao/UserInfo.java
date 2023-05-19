package io.aiven.klaw.dao;

import io.aiven.klaw.helpers.SwitchTeamsConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kwusers")
public class UserInfo implements Serializable {

  @Id
  @Column(name = "userid")
  private String username;

  @Column(name = "tenantid")
  private int tenantId;

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

  @Column(name = "switchteams")
  private boolean switchTeams;

  @Column(name = "switchallowedteams")
  @Convert(converter = SwitchTeamsConverter.class)
  private Set<Integer> switchAllowedTeamIds;

  @Column(name = "otherparams")
  private String otherParams;

  @Column(name = "resettoken")
  private String resetToken;

  @Column(name = "resettokengeneratedat")
  private Timestamp resetTokenGeneratedAt;
}
