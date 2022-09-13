package com.kafkamgt.uiapi.model;

public enum AclType {
    Producer ("Producer"),
    Consumer("Consumer");

    public final String value;
    private AclType(String value) {
        this.value = value;
    }
}
