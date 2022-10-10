package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;

@Builder(toBuilder = true)
public class ClusterSchemaRequest implements Serializable {

  @JsonProperty private String env;

  @JsonProperty private String protocol;

  @JsonProperty private String topicName;

  @JsonProperty private String fullSchema;
}
