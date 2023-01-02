package io.aiven.klaw.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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

  private String pwd;

  private String team;

  private Integer teamId;

  private String role;

  @Size(min = 5, max = 50, message = "Name must be above 4 characters")
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
