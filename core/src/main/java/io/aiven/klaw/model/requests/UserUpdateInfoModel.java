package io.aiven.klaw.model.requests;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX_VALIDATION_STR;
import static io.aiven.klaw.helpers.KwConstants.PASSWORD_UPDATE_REGEX;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
  @Pattern(regexp = PASSWORD_UPDATE_REGEX, message = PASSWORD_REGEX_VALIDATION_STR)
  @NotNull
  private String userPassword;

  @NotNull private Integer teamId;

  @NotNull private boolean switchTeams;

  private Set<Integer> switchAllowedTeamIds;

  private Set<String> switchAllowedTeamNames;

  private int tenantId;
}
