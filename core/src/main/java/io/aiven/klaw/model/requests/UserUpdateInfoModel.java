package io.aiven.klaw.model.requests;

import io.aiven.klaw.validation.PasswordUpdateValidator;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class UserUpdateInfoModel extends ProfileModel implements Serializable {
  // Can't be changed in update so no need to require a more restrictive check.
  @NotNull(message = "Username cannot be null")
  private String username;

  @NotNull private String role;

  // Update regex check also allows the masked pw tobe sent.
  @PasswordUpdateValidator private String userPassword;

  @NotNull private Integer teamId;

  @NotNull private boolean switchTeams;

  private Set<Integer> switchAllowedTeamIds;

  private Set<String> switchAllowedTeamNames;

  private int tenantId;
}
