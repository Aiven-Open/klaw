package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.aiven.klaw.model.KafkaSupportedProtocol;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ClusterSchemaRequest implements Serializable {

  @JsonProperty private String env;

  @JsonProperty private KafkaSupportedProtocol protocol;

  @JsonProperty private String topicName;

  @JsonProperty private String fullSchema;

  @JsonProperty private String clusterIdentification;

  @JsonProperty private boolean forceRegister;
}
