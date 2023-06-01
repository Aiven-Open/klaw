package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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

  @Column(name = "schemaid")
  private Integer schemaId;

  @Column(name = "compatibility")
  private String compatibility;

  @Column(name = "jsonparams")
  private String jsonParams;
}
