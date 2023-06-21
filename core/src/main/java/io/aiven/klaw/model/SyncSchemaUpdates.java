package io.aiven.klaw.model;

import java.util.List;
import lombok.Data;

@Data
public class SyncSchemaUpdates {
  //  Schema & Topic share the same name
  //  topicList is for the addition of new schemas
  private List<String> topicList;
  private List<String>
      topicListForRemoval; // A list of schema to be removed from Klaw metadata, schemas are
  // referenced by the topic that wns them. so it is in fact a list of topics
  // names.
  private String sourceKafkaEnvSelected;
  private String targetKafkaEnvSelected;
  private String topicsSelectionType; // ALL_TOPICS / SELECTED_TOPICS
  private String typeOfSync; // SYNC_SCHEMAS / SYNC_BACK_SCHEMAS
  private boolean forceRegisterSchema;
}
