package io.aiven.klaw.model.enums;

public enum RequestStatus {
  CREATED("created"),
  DELETED("deleted"),
  DECLINED("declined"),
  APPROVED("approved"),
  ALL("all");

  public final String value;

  RequestStatus(String value) {
    this.value = value;
  }
}
