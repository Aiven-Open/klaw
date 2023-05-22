package io.aiven.klaw.model.cluster;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SchemasInfoOfClusterResponse implements Serializable {
  private List<SchemaInfoOfTopic> schemaInfoOfTopicList;
}
