package io.aiven.klaw.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum KafkaFlavors {
  APACHE_KAFKA("Apache Kafka"),
  AIVEN_FOR_APACHE_KAFKA("Aiven for Apache Kafka"),
  CONFLUENT("Confluent"),
  OTHERS("Others");

  public final String value;

  KafkaFlavors(String value) {
    this.value = value;
  }

  @Nullable
  public static KafkaFlavors of(@Nullable String value) {
    for (KafkaFlavors kafkaFlavors : values()) {
      if (kafkaFlavors.value.equals(value)) {
        return kafkaFlavors;
      }
    }
    log.warn("Unknown KafkaFlavors value '{}'", value);
    return null;
  }
}
