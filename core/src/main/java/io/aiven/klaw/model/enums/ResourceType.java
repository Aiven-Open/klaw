package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum ResourceType {
  KAFKA("Kafka"),
  ACL("Acl"),
  SCHEMA("Schema"),
  CONNECTOR("Connector"),
  UNKNOWN("Unknown");

  public final String value;

  ResourceType(String value) {
    this.value = value;
  }

  @Nullable
  public ResourceType of(@Nullable String value) {
    for (ResourceType val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    log.warn("Unknown ResourceType value '{}'", value);
    return ResourceType.UNKNOWN;
  }
}
