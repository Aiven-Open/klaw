package io.aiven.klaw.clusterapi.models.consumergroup;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Types of consumer group offsets to reset */
@Getter
@AllArgsConstructor
public enum OffsetResetType {
  LATEST("LATEST"),

  EARLIEST("EARLIEST"),

  TO_DATE_TIME("TO_DATE_TIME");

  private final String value;
}
