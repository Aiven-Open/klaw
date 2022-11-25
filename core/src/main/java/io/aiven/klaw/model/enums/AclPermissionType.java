package io.aiven.klaw.model.enums;

public enum AclPermissionType {
  WRITE("WRITE"),
  READ("READ");

  public final String value;

  AclPermissionType(String value) {
    this.value = value;
  }
}
