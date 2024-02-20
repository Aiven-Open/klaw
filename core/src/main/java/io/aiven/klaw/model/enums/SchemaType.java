package io.aiven.klaw.model.enums;

public enum SchemaType {
  AVRO("AVRO"),
  JSON("JSON");

  public final String value;

  SchemaType(String value) {
    this.value = value;
  }
}
