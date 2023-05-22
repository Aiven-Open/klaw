package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class SchemaSubjectInfoResponse implements Serializable {
  private String topic;
  private Set<Integer> schemaVersions;

  private String teamname;

  private int teamId;

  private List<String> possibleTeams;

  private String remarks;

  @NotNull private String currentPage;

  @NotNull private String totalNoPages;

  @NotNull private List<String> allPageNos;
}
