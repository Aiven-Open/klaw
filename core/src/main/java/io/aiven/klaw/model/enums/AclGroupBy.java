package io.aiven.klaw.model.enums;

public enum AclGroupBy {
  NONE("NONE"),
  TEAM("TEAM");

  public final String value;

  AclGroupBy(String value) {
    this.value = value;
  }
}
