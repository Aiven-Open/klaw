package io.aiven.klaw.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ConnectorOverview {
  List<KafkaConnectorModel> topicInfoList;
  private List<TopicHistory> topicHistoryList;
  Map<String, String> promotionDetails;
  boolean connectorExists;

  String topicDocumentation;
  Integer topicIdForDocumentation;
}
