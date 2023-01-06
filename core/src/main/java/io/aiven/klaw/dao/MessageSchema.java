package io.aiven.klaw.dao;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(MessageSchemaID.class)
@Table(name = "kwavroschemas")
public class MessageSchema implements Serializable {

  @Id
  @Column(name = "avroschemaid")
  private Integer req_no;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "topicname")
  private String topicname;

  @Column(name = "env")
  private String environment;

  @Column(name = "versionschema")
  private String schemaversion;

  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "schemafull")
  private String schemafull;

  @Column(name = "jsonparams")
  private String jsonParams;
}
