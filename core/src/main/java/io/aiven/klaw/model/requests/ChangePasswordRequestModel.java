package io.aiven.klaw.model.requests;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX;
import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX_VALIDATION_STR;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequestModel implements Serializable {

  @NotNull
  @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_REGEX_VALIDATION_STR)
  String pwd;

  @NotNull
  @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_REGEX_VALIDATION_STR)
  String repeatPwd;
}
