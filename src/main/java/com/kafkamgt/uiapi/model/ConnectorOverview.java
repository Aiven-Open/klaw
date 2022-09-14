package com.kafkamgt.uiapi.model;

import java.util.HashMap;
import java.util.List;
import lombok.Data;

@Data
public class ConnectorOverview {
  List<KafkaConnectorModel> topicInfoList;
  private List<TopicHistory> topicHistoryList;
  HashMap<String, String> promotionDetails;
  boolean connectorExists;

  String topicDocumentation;
  Integer topicIdForDocumentation;
}
