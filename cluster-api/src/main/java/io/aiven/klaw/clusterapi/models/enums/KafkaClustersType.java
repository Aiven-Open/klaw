package io.aiven.klaw.clusterapi.models.enums;

public enum KafkaClustersType {
  KAFKA("kafka"),
  SCHEMA_REGISTRY("schemaregistry"),
  KAFKA_CONNECT("kafkaconnect");

  public final String value;

  private KafkaClustersType(String value) {
    this.value = value;
  }
}
