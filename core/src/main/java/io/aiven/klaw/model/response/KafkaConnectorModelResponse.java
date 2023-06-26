package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class KafkaConnectorModelResponse {

  private Integer sequence;

  @NotNull private Integer connectorId;

  @NotNull private String connectorName;

  @NotNull private String connectorStatus;

  @NotNull private long runningTasks;

  @NotNull private long failedTasks;

  @NotNull private String environmentId;

  @NotNull private String teamName;

  @NotNull private int teamId;

  @NotNull private boolean showEditConnector;

  @NotNull private boolean showDeleteConnector;

  @NotNull private boolean connectorDeletable;

  private List<String> allPageNos;

  private String totalNoPages;

  private String currentPage;

  private List<EnvIdInfo> environmentsList;

  private List<String> possibleTeams;

  private String connectorConfig;

  private String environmentName;

  private String remarks;

  private String documentation;

  private String description;
}
