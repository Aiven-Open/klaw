package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteAclRequestModel {
  @NotNull String requestId;
}
