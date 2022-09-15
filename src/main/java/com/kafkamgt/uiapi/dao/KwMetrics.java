package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;

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
