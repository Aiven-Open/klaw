package io.aiven.klaw.clusterapi.models;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClusterSchemaRequest implements Serializable {

  private String env;

  private KafkaSupportedProtocol protocol;

  private String topicName;

  private String fullSchema;
}
