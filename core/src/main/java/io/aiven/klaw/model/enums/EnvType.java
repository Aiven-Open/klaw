package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum EnvType {
  KAFKA("kafka"),
  KAFKACONNECT("kafkaconnect"),
  SCHEMAREGISTRY("schemaregistry");

  public final String value;

  EnvType(String value) {
    this.value = value;
  }

  @Nullable
  public static EnvType of(@Nullable String value) {
    for (EnvType val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    log.warn("Unknown EnvType value '{}'", value);
    return null;
  }
}
