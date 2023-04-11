package io.aiven.klaw.model.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterUserInfoModelResponse implements Serializable {

  @NotNull private String username;

  @NotNull private String fullname;

  @Email private String mailid;

  private String pwd;

  private String team;

  private Integer teamId;

  private String role;

  private String status;

  private Timestamp registeredTime;

  private String approver;

  private String registrationId;

  private int tenantId;

  private String tenantName;
}
