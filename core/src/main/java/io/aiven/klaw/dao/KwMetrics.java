package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@Table(name = "kwkafkametrics")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KwMetrics implements Serializable {

  @Id
  @Column(name = "metricsid")
  private Integer metricsId;

  @Column(name = "metricstime")
  private String metricsTime;

  @Column(name = "env")
  private String env;

  @Column(name = "metricstype")
  private String metricsType;

  @Column(name = "metricsname")
  private String metricsName;

  @Column(name = "metricsattributes")
  private String metricsAttributes;
}
