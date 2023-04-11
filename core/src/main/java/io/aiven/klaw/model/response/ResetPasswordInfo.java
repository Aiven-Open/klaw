package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class ResetPasswordInfo {
  private String passwordSent;
  private String userFound;
}
