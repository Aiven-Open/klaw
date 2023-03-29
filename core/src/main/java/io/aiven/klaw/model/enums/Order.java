package io.aiven.klaw.model.enums;

public enum Order {
  ASC_REQUESTED_TIME("asc_requested_time"),
  DESC_REQUESTED_TIME("desc_requested_time");

  public final String value;

  Order(String value) {
    this.value = value;
  }
}
