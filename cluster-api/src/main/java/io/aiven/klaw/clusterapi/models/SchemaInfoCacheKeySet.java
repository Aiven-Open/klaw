package io.aiven.klaw.clusterapi.models;

import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import lombok.Data;

@Data
public class SchemaInfoCacheKeySet {
  private String bootstrapServers;
  private KafkaSupportedProtocol protocol;
  private String clusterIdentification;
}
