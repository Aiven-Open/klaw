package io.aiven.klaw.clusterapi.models;

import java.util.Set;
import lombok.Data;

@Data
public class SchemaOfTopic {
  String topic;
  Set<Integer> schemaVersions;
}
