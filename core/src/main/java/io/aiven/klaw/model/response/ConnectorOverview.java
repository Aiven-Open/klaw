package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicHistory;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ConnectorOverview {
  @NotNull List<KafkaConnectorModelResponse> connectorInfoList;
  private List<TopicHistory> topicHistoryList;
  Map<String, String> promotionDetails;
  @NotNull boolean connectorExists;

  String topicDocumentation;
  Integer topicIdForDocumentation;
}
