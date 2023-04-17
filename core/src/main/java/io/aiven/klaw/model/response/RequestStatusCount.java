package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.RequestStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestStatusCount {
  RequestStatus requestStatus; // CREATED,DELETED,APPROVED,DECLINED
  long count;
}
