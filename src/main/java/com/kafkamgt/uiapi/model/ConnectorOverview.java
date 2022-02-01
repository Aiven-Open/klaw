package com.kafkamgt.uiapi.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class ConnectorOverview {
    List<KafkaConnectorModel> topicInfoList;
    private List<TopicHistory> topicHistoryList;
    HashMap<String, String> promotionDetails;
    boolean connectorExists;

    String topicDocumentation;
    Integer topicIdForDocumentation;
}
