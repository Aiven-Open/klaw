package io.aiven.klaw.clusterapi.models;

import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.SchemaType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClusterSchemaRequest implements Serializable {

  private String env;

  private KafkaSupportedProtocol protocol;

  private String topicName;

  private String fullSchema;

  private String clusterIdentification;

  @NotNull private SchemaType schemaType;

  private boolean forceRegister;
}
