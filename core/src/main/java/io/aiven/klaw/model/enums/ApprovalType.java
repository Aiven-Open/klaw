package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum ApprovalType {
  TOPIC_TEAM_OWNER,
  CONNECTOR_TEAM_OWNER,
  ACL_TEAM_OWNER,
  TEAM;

  @Nullable
  public static ApprovalType of(@Nullable String value) {
    for (ApprovalType val : values()) {
      if (val.name().equals(value)) {
        return val;
      }
    }
    log.warn("Unknown ApprovalType value '{}'", value);
    return null;
  }

  public static boolean isApprovalType(@Nullable String value) {
    return of(value) != null;
  }
}
