package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum RequestStatus {
  CREATED("created"),
  DELETED("deleted"),
  DECLINED("declined"),
  APPROVED("approved"),
  ALL("all");

  public final String value;

  RequestStatus(String value) {
    this.value = value;
  }

  @Nullable
  public static RequestStatus of(@Nullable String value) {
    for (RequestStatus val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    log.warn("Unknown RequestStatus value '{}'", value);
    return null;
  }
}
