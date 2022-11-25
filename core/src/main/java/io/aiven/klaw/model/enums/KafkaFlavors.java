package io.aiven.klaw.model.enums;

public enum KafkaFlavors {
  APACHE_KAFKA("Apache Kafka"),
  AIVEN_FOR_APACHE_KAFKA("Aiven for Apache Kafka"),
  CONFLUENT("Confluent"),
  OTHERS("Others");

  public final String value;

  KafkaFlavors(String value) {
    this.value = value;
  }
}
