package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class SchemaDetailsResponse {
  private String schemaContent;
  private String topicName;
  private String schemaVersion;
  private String envName;
}
