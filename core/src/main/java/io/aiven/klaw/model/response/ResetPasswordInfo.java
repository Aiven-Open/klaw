package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordInfo {

  @NotNull private String passwordSent;

  @NotNull private String userFound;
}
