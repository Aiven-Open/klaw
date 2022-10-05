package io.aiven.klaw.model;

public enum AclPatternType {
  PREFIXED("PREFIXED"),
  LITERAL("LITERAL");

  public final String value;

  AclPatternType(String value) {
    this.value = value;
  }
}
