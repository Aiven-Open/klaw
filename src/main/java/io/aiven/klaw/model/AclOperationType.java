package io.aiven.klaw.model;

public enum AclOperationType {
  CREATE("Create"),
  DELETE("Delete");

  public final String value;

  AclOperationType(String value) {
    this.value = value;
  }
}
