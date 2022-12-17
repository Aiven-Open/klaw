package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.aiven.klaw.model.KafkaSupportedProtocol;
import java.io.Serializable;
import lombok.Builder;

@Builder(toBuilder = true)
public class ClusterConnectorRequest implements Serializable {

  @JsonProperty private String env;

  @JsonProperty private KafkaSupportedProtocol protocol;

  @JsonProperty private String connectorConfig;

  @JsonProperty private String connectorName;

  @JsonProperty private String clusterIdentification;
}
