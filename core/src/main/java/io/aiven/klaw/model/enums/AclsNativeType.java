package io.aiven.klaw.model.enums;

public enum AclsNativeType {
  NATIVE("NATIVE"),
  AIVEN("AIVEN"),
  CONFLUENT_CLOUD("CONFLUENT_CLOUD");

  public final String value;

  AclsNativeType(String value) {
    this.value = value;
  }
}
