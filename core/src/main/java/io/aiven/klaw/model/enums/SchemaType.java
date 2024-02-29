package io.aiven.klaw.model.enums;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum SchemaType {
  AVRO("AVRO"),
  JSON("JSON");

  public final String value;

  SchemaType(String value) {
    this.value = value;
  }

  @Nullable
  public static SchemaType of(@Nullable String value) {
    for (SchemaType val : values()) {
      if (val.value.equals(value)) {
        return val;
      }
    }
    return null;
  }
}
