package io.aiven.klaw.model.enums;

public enum AclGroupBy {
  ACL_TYPE("ACL_TYPE"),
  ENV("ENV"),
  TEAM("TEAM"),
  IP("IP"),
  PRINCIPAL("PRINCIPAL");

  public final String value;

  AclGroupBy(String value) {
    this.value = value;
  }
}
