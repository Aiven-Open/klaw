package io.aiven.klaw.clusterapi.models.enums;

public enum SchemaCacheUpdateType {
  CREATE("CREATE"),
  DELETE("DELETE"),
  NONE("NONE");

  public final String value;

  SchemaCacheUpdateType(String value) {
    this.value = value;
  }
}
