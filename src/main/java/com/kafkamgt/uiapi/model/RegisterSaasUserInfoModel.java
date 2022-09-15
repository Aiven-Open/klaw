package com.kafkamgt.uiapi.model;

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
public class RegisterSaasUserInfoModel implements Serializable {

  @Size(min = 4, max = 300, message = "Name must be above 3 characters")
  @NotNull(message = "Name cannot be null")
  @Pattern(message = "Invalid Full name.", regexp = "^[a-zA-z ]*$") // Pattern a-zA-z and/or spaces.
  private String fullname;

  @Email(message = "Email should be valid")
  private String mailid;

  private String recaptchaStr;

  private String status;

  private Timestamp registeredTime;

  private String approver;

  private String registrationId;

  private String tenantName;
}
