package io.aiven.klaw.clusterapi.models.enums;

public enum AclAttributes {
  TOPIC("topic"),
  PERMISSION("permission"),
  USERNAME("username");

  public final String value;

  AclAttributes(String value) {
    this.value = value;
  }
}
