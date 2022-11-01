package io.aiven.klaw.model;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum TopicRequestTypes {
  Create,
  Update,
  Delete,
  Claim;

  @Nullable
  public static TopicRequestTypes of(@Nullable String value) {
    for (TopicRequestTypes val : values()) {
      if (val.name().equals(value)) {
        return val;
      }
    }
    log.warn("Unknown TopicRequestTypes value '{}'", value);
    return null;
  }
}
