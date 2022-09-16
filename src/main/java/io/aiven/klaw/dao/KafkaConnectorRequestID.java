package io.aiven.klaw.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class KafkaConnectorRequestID implements Serializable {

  private Integer connectorId;

  private Integer tenantId;

  public KafkaConnectorRequestID() {}

  public KafkaConnectorRequestID(Integer connectorId, Integer tenantId) {
    this.connectorId = connectorId;
    this.tenantId = tenantId;
  }
}
