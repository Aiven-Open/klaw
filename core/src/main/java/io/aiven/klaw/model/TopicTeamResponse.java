package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicTeamResponse {
  private String team;

  private int teamId;

  private String error;

  @NotNull private boolean status;
}
