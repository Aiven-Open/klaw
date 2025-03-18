package io.aiven.klaw.model.requests;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX;
import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX_VALIDATION_STR;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterUserInfoModel implements Serializable {

  // Can be emailid or username
  @Size(min = 6, max = 300, message = "UserName must be above 5 characters")
  @NotNull(message = "Username cannot be null")
  private String username;

  @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_REGEX_VALIDATION_STR)
  @NotNull
  private String pwd;

  private String team;

  private Integer teamId;

  private String role;

  @Size(min = 5, max = 50, message = "Name must be atleast 5 characters")
  @NotNull(message = "Name cannot be null")
  @Pattern(
      message = "Invalid Full name.",
      regexp = "^[a-zA-z ]*$") //  Pattern a-zA-z and/or spaces.
  private String fullname;

  @Email(message = "Email should be valid")
  private String mailid;

  private String status;

  private Timestamp registeredTime;

  private String approver;

  private String registrationId;

  private int tenantId;

  private String tenantName;
}
