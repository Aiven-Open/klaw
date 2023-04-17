package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class TenantInfo {
  private Integer tenants;

  private Integer teams;

  private Integer clusters;

  private Integer topics;
}
