package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;

@Builder(toBuilder = true)
public class ClusterConnectorRequest implements Serializable {

  @JsonProperty private String env;

  @JsonProperty private String protocol;

  @JsonProperty private String connectorConfig;

  @JsonProperty private String connectorName;
}
