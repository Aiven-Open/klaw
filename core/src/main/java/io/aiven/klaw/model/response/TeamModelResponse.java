package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamModelResponse implements Serializable {

  @NotNull private String teamname;

  @NotNull private String teamphone;

  @NotNull private String contactperson;

  @NotNull private Integer teamId;

  @NotNull private Integer tenantId;

  @NotNull private boolean showDeleteTeam;

  @NotNull private String tenantName;

  private String app;

  private String teammail;

  private List<String> envList;
}
