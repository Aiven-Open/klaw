package io.aiven.klaw.model.cluster.consumergroup;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OffsetsTiming {
  BEFORE_OFFSET_RESET("BEFORE_OFFSET_RESET"),

  AFTER_OFFSET_RESET("AFTER_OFFSET_RESET");

  private final String value;
}
