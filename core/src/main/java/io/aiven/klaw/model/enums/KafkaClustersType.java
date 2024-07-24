package io.aiven.klaw.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum KafkaClustersType {
  ALL("all"),
  KAFKA("kafka"),
  SCHEMA_REGISTRY("schemaregistry"),
  KAFKA_CONNECT("kafkaconnect");

  public final String value;

  private static final Map<String, KafkaClustersType> lookup = new HashMap<>();

  // Static block to populate the lookup map
  static {
    for (KafkaClustersType type : KafkaClustersType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  public String getValue() {
    return value;
  }

  KafkaClustersType(String value) {
    this.value = value;
  }

  public static KafkaClustersType of(String name) {
    KafkaClustersType type = lookup.get(name);
    if (type == null) {
      throw new IllegalArgumentException("Unknown KafkaClustersType name " + name);
    }

    return type;
  }
}
