package com.kafkamgt.uiapi.model;

public enum KafkaFlavors {
    Apache_Kafka("Apache Kafka"),
    Aiven_For_Apache_Kafka("Aiven for Apache Kafka"),
    Confluent("Confluent"),
    Others("Others");

    public final String value;
    private KafkaFlavors(String value) {
        this.value = value;
    }
}
