package io.aiven.klaw.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantConfig implements Serializable {
  private KwTenantConfigModel tenantModel;
}
