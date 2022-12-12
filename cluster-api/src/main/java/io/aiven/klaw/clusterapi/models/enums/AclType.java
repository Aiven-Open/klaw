package io.aiven.klaw.clusterapi.models.enums;

public enum AclType {
  PRODUCER("Producer"),
  CONSUMER("Consumer");

  public final String value;

  AclType(String value) {
    this.value = value;
  }
}
