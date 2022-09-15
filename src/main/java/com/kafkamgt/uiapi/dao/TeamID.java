package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class TeamID implements Serializable {

  private Integer teamId;

  private Integer tenantId;

  public TeamID() {}

  public TeamID(Integer teamId, Integer tenantId) {
    this.teamId = teamId;
    this.tenantId = tenantId;
  }
}
