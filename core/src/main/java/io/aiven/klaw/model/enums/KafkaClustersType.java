package io.aiven.klaw.model.enums;

public enum KafkaClustersType {
  ALL("all"),
  KAFKA("kafka"),
  SCHEMA_REGISTRY("schemaregistry"),
  KAFKA_CONNECT("kafkaconnect");

  public final String value;

  KafkaClustersType(String value) {
    this.value = value;
  }

  public static KafkaClustersType of(String name) {
    for (KafkaClustersType val : values()) {
      if (val.value.equals(name)) {
        return val;
      }
    }
    throw new IllegalArgumentException("Unknown KafkaClustersType name " + name);
  }
}
