package io.aiven.klaw.model;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum EntityType {
  TEAM,
  ENVIRONMENT,
  CLUSTER,
  TENANT,
  ROLES_PERMISSIONS,
  PROPERTIES;

  @Nullable
  public static EntityType of(@Nullable String value) {
    for (EntityType val : values()) {
      if (val.name().equals(value)) {
        return val;
      }
    }
    log.warn("Unknown EntityType value '{}'", value);
    return null;
  }
}
