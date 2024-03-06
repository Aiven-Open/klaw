package io.aiven.klaw.model.enums;

public enum OrderBy {
  ASC("ASC"),
  DESC("DESC");

  public final String value;

  OrderBy(String value) {
    this.value = value;
  }
}
