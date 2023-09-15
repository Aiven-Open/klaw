package io.aiven.klaw.model.response;

import io.aiven.klaw.model.ResourceHistory;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ConnectorOverview {
  @NotNull List<KafkaConnectorModelResponse> connectorInfoList;
  private List<ResourceHistory> connectorHistoryList;
  @NotNull ConnectorPromotionStatus promotionDetails;
  @NotNull boolean connectorExists;
  @NotNull private List<EnvIdInfo> availableEnvironments;
  String connectorDocumentation;
  @NotNull Integer connectorIdForDocumentation;
}
