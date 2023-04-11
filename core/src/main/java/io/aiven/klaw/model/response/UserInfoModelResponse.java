package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class UserInfoModelResponse implements Serializable {

  @NotNull private String username;

  @NotNull private String fullname;

  @NotNull private String mailid;

  @NotNull private String role;

  @NotNull private boolean switchTeams;

  @NotNull private String team;

  @NotNull private Integer teamId;

  @NotNull private int tenantId;

  private String userPassword;

  private Set<Integer> switchAllowedTeamIds;

  private Set<String> switchAllowedTeamNames;

  private String totalNoPages;

  private List<String> allPageNos;
}
