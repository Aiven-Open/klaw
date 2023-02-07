package io.aiven.klaw.model.enums;

public enum RequestEntityType {
  TOPIC("topic"),
  ACL("acl"),
  SCHEMA("schema"),
  CONNECTOR("connector"),
  USER("user");

  public final String value;

  RequestEntityType(String value) {
    this.value = value;
  }
}
