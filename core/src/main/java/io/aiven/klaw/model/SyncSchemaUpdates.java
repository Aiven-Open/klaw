package io.aiven.klaw.model;

import java.util.List;
import lombok.Data;

@Data
public class SyncSchemaUpdates {
  private List<String> topicList;
  private String kafkaEnvSelected;
}
