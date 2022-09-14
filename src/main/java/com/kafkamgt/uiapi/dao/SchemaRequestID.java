package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SchemaRequestID implements Serializable {

  private Integer req_no;

  private Integer tenantId;

  public SchemaRequestID() {}

  public SchemaRequestID(Integer req_no, Integer tenantId) {
    this.req_no = req_no;
    this.tenantId = tenantId;
  }
}
