package io.aiven.klaw.clusterapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClusterTopicRequest {
  @JsonProperty private String env;

  @JsonProperty private int partitions;

  @JsonProperty private short replicationFactor;

  @JsonProperty private KafkaSupportedProtocol protocol;

  @JsonProperty private String clusterName;

  @JsonProperty private String topicName;

  @JsonProperty private Map<String, String> advancedTopicConfiguration;
}
