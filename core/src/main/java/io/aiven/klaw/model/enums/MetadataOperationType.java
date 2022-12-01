package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum MetadataOperationType {
  CREATE,
  DELETE,
  UPDATE,
  INSERT;

  @Nullable
  public static MetadataOperationType of(@Nullable String value) {
    for (MetadataOperationType val : values()) {
      if (val.name().equals(value)) {
        return val;
      }
    }
    log.warn("Unknown MetadataOperationType value '{}'", value);
    return null;
  }
}
