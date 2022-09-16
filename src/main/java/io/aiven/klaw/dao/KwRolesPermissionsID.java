package io.aiven.klaw.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class KwRolesPermissionsID implements Serializable {

  private Integer id;

  private Integer tenantId;

  public KwRolesPermissionsID() {}

  public KwRolesPermissionsID(Integer id, Integer tenantId) {
    this.id = id;
    this.tenantId = tenantId;
  }
}
