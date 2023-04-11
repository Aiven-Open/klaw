package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.RequestOperationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestsOperationTypeCount {
  RequestOperationType requestOperationType; // CREATE,DELETE,CLAIM,UPDATE
  long count;
}
