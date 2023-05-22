package io.aiven.klaw.clusterapi.models;

import java.util.List;
import lombok.Data;

@Data
public class SchemasOfClusterResponse {
  List<SchemaOfTopic> schemaOfTopicList;
}
