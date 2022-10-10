package io.aiven.klaw.model.cluster;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;

@Builder(toBuilder = true)
public class ClusterTopicRequest implements Serializable {

  @JsonProperty private String env;

  @JsonProperty private int partitions;

  @JsonProperty private short replicationFactor;

  @JsonProperty private String protocol;

  @JsonProperty private String clusterName;

  @JsonProperty private String topicName;
}
