package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum AclType {
  PRODUCER("Producer"),
  CONSUMER("Consumer");

  public final String value;

  @Nullable
  public static AclType of(@Nullable String value) {
    for (AclType val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    log.warn("Unknown AclType value '{}'", value);
    return null;
  }

  AclType(String value) {
    this.value = value;
  }
}
