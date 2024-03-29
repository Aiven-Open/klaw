package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum EntityType {
  USERS,
  TEAM,
  ENVIRONMENT,
  CLUSTER,
  TENANT,
  ROLES_PERMISSIONS,
  PROPERTIES,
  TOPICS;

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
