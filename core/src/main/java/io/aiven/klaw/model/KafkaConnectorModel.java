package io.aiven.klaw.model;

import java.util.List;
import lombok.Data;

@Data
public class KafkaConnectorModel {

  int sequence;

  int connectorId;

  String connectorName;

  String connectorConfig;

  String environmentName;

  String environmentId;

  String teamName;

  private List<String> possibleTeams;

  private List<String> allPageNos;

  private String totalNoPages;

  private String currentPage;

  private String remarks;

  private String documentation;

  private List<String> environmentsList;

  private String description;

  private boolean showEditConnector;

  private boolean showDeleteConnector;

  private boolean ConnectorDeletable;
}
