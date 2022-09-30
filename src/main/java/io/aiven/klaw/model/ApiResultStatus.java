package io.aiven.klaw.model;

public enum ApiResultStatus {
  SUCCESS("success"),
  FAILURE("failure"),
  NOT_AUTHORIZED("Not Authorized"),
  ERROR("error");

  public final String value;

  ApiResultStatus(String value) {
    this.value = value;
  }
}
