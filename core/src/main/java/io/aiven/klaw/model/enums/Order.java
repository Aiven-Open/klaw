package io.aiven.klaw.model.enums;

public enum Order {
  OLDEST_FIRST("oldest_first"),
  NEWEST_FIRST("newest_first");

  public final String value;

  Order(String value) {
    this.value = value;
  }
}
