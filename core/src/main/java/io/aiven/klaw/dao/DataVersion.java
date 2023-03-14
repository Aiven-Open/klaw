package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Data;

@Data
@Table(name = "kwdatamigration")
public class DataVersion implements Serializable {
  @Column(name = "version")
  @Id
  private String version;

  @Column(name = "executedat")
  private Timestamp executedAt;

  @Column(name = "complete")
  private boolean complete;
}
