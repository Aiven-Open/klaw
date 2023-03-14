package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KafkaConnectorRequestsResponseModel extends BaseRequestsResponseModel
    implements Serializable {

  @NotNull private String connectorName;

  @NotNull private String description;

  @NotNull private String connectorConfig;

  @NotNull private Integer connectorId;

  private List<String> possibleTeams;

  private String approvingTeamId;

  private String otherParams;
}
