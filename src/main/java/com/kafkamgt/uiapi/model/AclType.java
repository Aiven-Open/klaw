package com.kafkamgt.uiapi.model;

public enum AclType {
    PRODUCER("Producer"),
    CONSUMER("Consumer");

    public final String value;
    private AclType(String value) {
        this.value = value;
    }
}
