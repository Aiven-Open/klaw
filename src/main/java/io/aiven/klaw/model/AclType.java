package io.aiven.klaw.model;

public enum AclType {
  PRODUCER("Producer"),
  CONSUMER("Consumer");

  public final String value;

  AclType(String value) {
    this.value = value;
  }
}
