package io.aiven.klaw.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemaPromotion {

  private String targetEnvironment;
  private String sourceEnvironment;
  private String topicName;
  private String schemaVersion;
  private String schemaFull;

  private boolean forceRegister;
  private String appName;

  private String remarks;
}
