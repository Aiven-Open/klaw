package io.aiven.klaw.clusterapi.models;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClusterConnectorRequest implements Serializable {

  @NotNull private String env;

  @NotNull private KafkaSupportedProtocol protocol;

  @NotNull private String connectorConfig;

  @NotNull private String connectorName;

  @NotNull private String clusterIdentification;
}
