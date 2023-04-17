package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.enums.RequestEntityType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RequestVerdict {

  @Nullable
  // Reason for declining a request only required on declining Requests
  private String reason;

  @NotNull private RequestEntityType requestEntityType;
  @NotNull private List<String> reqIds;
}
