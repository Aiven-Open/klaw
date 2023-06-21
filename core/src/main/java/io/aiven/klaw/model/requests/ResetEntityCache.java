package io.aiven.klaw.model.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetEntityCache {
  private int tenantId;
  private String entityType;
  private String entityValue;
  private String operationType;
}
