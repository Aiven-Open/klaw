package io.aiven.klaw.model.requests;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX;

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequestModel implements Serializable {

  @Pattern(
      regexp = PASSWORD_REGEX,
      message =
          "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.")
  String pwd;

  @Pattern(
      regexp = PASSWORD_REGEX,
      message =
          "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.")
  String repeatPwd;
}
