package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicHistory;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ConnectorOverview {
  List<KafkaConnectorModelResponse> connectorInfoList;
  private List<TopicHistory> topicHistoryList;
  Map<String, String> promotionDetails;
  boolean connectorExists;

  String topicDocumentation;
  Integer topicIdForDocumentation;
}
