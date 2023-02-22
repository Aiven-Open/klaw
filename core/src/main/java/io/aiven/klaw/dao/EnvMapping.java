package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(EnvID.class)
@Table(name = "kwenvmapping")
public class EnvMapping implements Serializable {

  @Id
  @Column(name = "id")
  private String id;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Column(name = "envname")
  private String name;

  @Column(name = "schemaenvs")
  private List<EnvTag> schemaEnvs;

  @Column(name = "connectorenvs")
  private List<EnvTag> connectorEnvs;
}
