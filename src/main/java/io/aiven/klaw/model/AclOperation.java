package io.aiven.klaw.model;

public enum AclOperation {
  CREATE("Create"),
  DELETE("Delete");

  public final String value;

  AclOperation(String value) {
    this.value = value;
  }
}
