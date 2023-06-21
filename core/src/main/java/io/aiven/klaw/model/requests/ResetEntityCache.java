package io.aiven.klaw.model.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetEntityCache {
  private int tenantId;
  private String entityType;
  private String entityValue;
  private String operationType;
}
