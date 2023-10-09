package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum RequestOperationType {
  CREATE("Create"),
  UPDATE("Update"),
  PROMOTE("Promote"),
  CLAIM("Claim"),
  DELETE("Delete"),
  ALL("All"); // All is applicable only to query requests

  public final String value;

  RequestOperationType(String value) {
    this.value = value;
  }

  @Nullable
  public static RequestOperationType of(@Nullable String value) {
    for (RequestOperationType val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    log.warn("Unknown RequestOperationType value '{}'", value);
    return null;
  }
}
