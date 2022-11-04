package io.aiven.klaw.model;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class UserInfoModel implements Serializable {

  @Size(min = 6, max = 300, message = "Username must be above 5 characters")
  @NotNull(message = "Username cannot be null")
  //    @Pattern(message = "Invalid username", regexp = "^[A-Za-z0-9+_.-]+@(.+)$")
  private String username;

  private String userPassword;

  private String team;

  private Integer teamId;

  private String role;

  @NotNull(message = "Fullname cannot be null")
  @Size(min = 5, max = 50, message = "Name must be above 4 characters")
  @Pattern(message = "Invalid Full name", regexp = "^[a-zA-z ]*$")
  private String fullname;

  @Email(message = "Email should be valid")
  private String mailid;

  private String tenantName;

  private int tenantId;

  private String totalNoPages;
  private List<String> allPageNos;
}
