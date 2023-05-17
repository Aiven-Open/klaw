package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordInfo {

  @NotNull private String tokenSent;

  @NotNull private String userFound;
}
