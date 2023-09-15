package io.aiven.klaw.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class OperationalRequestID implements Serializable {

  private Integer reqId;

  private Integer tenantId;

  public OperationalRequestID() {}

  public OperationalRequestID(Integer reqId, Integer tenantId) {
    this.reqId = reqId;
    this.tenantId = tenantId;
  }
}
