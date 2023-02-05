package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum RequestOperationType {
  CREATE("Create"),
  UPDATE("Update"),
  CLAIM("Claim"),
  DELETE("Delete");

  public final String value;

  RequestOperationType(String value) {
    this.value = value;
  }

  @Nullable
  public static RequestOperationType of(@Nullable String value) {
    for (RequestOperationType val : values()) {
      if (val.name().equals(value)) {
        return val;
      }
    }
    log.warn("Unknown RequestOperationType value '{}'", value);
    return null;
  }
}
