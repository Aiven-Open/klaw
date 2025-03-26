package io.aiven.klaw.model.requests;

import io.aiven.klaw.validation.PasswordValidator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class UserInfoModel extends ProfileModel implements Serializable {

  @Size(min = 6, max = 300, message = "Username must be above 5 characters")
  @NotNull(message = "Username cannot be null")
  private String username;

  @NotNull private String role;

  @PasswordValidator @NotNull private String userPassword;

  @NotNull private Integer teamId;

  @NotNull private boolean switchTeams;

  private Set<Integer> switchAllowedTeamIds;

  private Set<String> switchAllowedTeamNames;

  private int tenantId;
}
