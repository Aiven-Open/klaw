package io.aiven.klaw.clusterapi.models;

public enum RequestOperationType {
  CREATE("Create"),
  UPDATE("Update"),
  DELETE("Delete");

  public final String value;

  RequestOperationType(String value) {
    this.value = value;
  }
}
