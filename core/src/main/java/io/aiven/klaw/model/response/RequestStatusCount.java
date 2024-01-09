package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestStatusCount {
  @NotNull RequestStatus requestStatus; // CREATED,DELETED,APPROVED,DECLINED

  @NotNull long count;
}
