package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum OperationalRequestType {
  RESET_CONSUMER_OFFSETS("RESET_CONSUMER_OFFSETS");

  public final String value;

  OperationalRequestType(String value) {
    this.value = value;
  }

  @Nullable
  public static OperationalRequestType of(@Nullable String value) {
    for (OperationalRequestType val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    log.warn("Unknown OperationalRequestType value '{}'", value);
    return null;
  }
}
