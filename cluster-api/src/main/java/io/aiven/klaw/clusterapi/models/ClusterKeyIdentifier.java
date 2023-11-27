package io.aiven.klaw.clusterapi.models;

import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterKeyIdentifier {
  private String bootstrapServers;
  private KafkaSupportedProtocol protocol;
  private String clusterIdentification;
}
