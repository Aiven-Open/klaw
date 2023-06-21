package io.aiven.klaw.model;

import java.util.List;
import lombok.Data;

@Data
public class SyncSchemaUpdates {
  private List<String> topicList;
  private List<String> schemaRemovalList; // A list of schema to be removed from Klaw metadata, schemas are referenced by the topic that wns them. so it is in fact a list of topics names.
  private String sourceKafkaEnvSelected;
  private String targetKafkaEnvSelected;
  private String topicsSelectionType; // ALL_TOPICS / SELECTED_TOPICS
  private String typeOfSync; // SYNC_SCHEMAS / SYNC_BACK_SCHEMAS
  private boolean forceRegisterSchema;
}
