package io.aiven.klaw.dao;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(KwKafkaConnectorID.class)
@Table(name = "kwkafkaconnector")
public class KwKafkaConnector implements Serializable {
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

  @Transient private List<String> environmentsList;

  @Column(name = "teamid")
  private Integer teamId;

  @Column(name = "description")
  private String description;

  @Column(name = "connectorconfig")
  private String connectorConfig;

  @Column(name = "documentation")
  private String documentation;

  @Column(name = "history")
  private String history;

  @Transient private boolean isExistingConnector;
}
