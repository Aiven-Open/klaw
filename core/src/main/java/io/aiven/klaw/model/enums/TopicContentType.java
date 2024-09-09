package io.aiven.klaw.model.enums;

import lombok.Getter;

@Getter
public enum TopicContentType {
  CUSTOM("custom"),
  RANGE("range");

  private final String value;

  TopicContentType(String value) {
    this.value = value;
  }
}
