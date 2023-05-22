package io.aiven.klaw.clusterapi.models;

import java.util.Set;
import lombok.Data;

@Data
public class SchemaInfoOfTopic {
  String topic;
  Set<Integer> schemaVersions;
}
