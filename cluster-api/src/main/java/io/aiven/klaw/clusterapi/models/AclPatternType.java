package io.aiven.klaw.clusterapi.models;

public enum AclPatternType {
  PREFIXED("PREFIXED"),
  LITERAL("LITERAL");

  public final String value;

  AclPatternType(String value) {
    this.value = value;
  }
}
