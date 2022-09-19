package io.aiven.klaw.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class KwPropertiesID implements Serializable {

  private String kwKey;

  private Integer tenantId;

  public KwPropertiesID() {}

  public KwPropertiesID(String kwKey, Integer tenantId) {
    this.kwKey = kwKey;
    this.tenantId = tenantId;
  }
}
