package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class EnvID implements Serializable {

  private String id;

  private Integer tenantId;

  public EnvID() {}

  public EnvID(String id, Integer tenantId) {
    this.id = id;
    this.tenantId = tenantId;
  }
}
