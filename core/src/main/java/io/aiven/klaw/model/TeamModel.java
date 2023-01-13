package io.aiven.klaw.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamModel implements Serializable {

  @NotNull
  @Pattern(
      message = "Invalid Team name. Pattern [a-zA-z 0-9] (Spaces allowed)",
      regexp = "^[a-zA-z 0-9]*$")
  private String teamname;

  @Email(message = "Email should be valid")
  private String teammail;

  @NotNull
  @Pattern(message = "Invalid Team phone.", regexp = "(^$|[0-9]*)")
  private String teamphone;

  @NotNull
  @Pattern(
      message = "Invalid Team contact.",
      regexp = "^[a-zA-z ]*$") // Pattern a-zA-z and/or spaces.
  private String contactperson;

  private Integer tenantId;

  private Integer teamId;

  private String app;

  private boolean showDeleteTeam;

  private String tenantName;

  private List<String> envList;
}
