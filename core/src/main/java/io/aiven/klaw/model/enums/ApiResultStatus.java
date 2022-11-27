package io.aiven.klaw.model.enums;

public enum ApiResultStatus {
  SUCCESS("success"),
  FAILURE("failure"),
  AUTHORIZED("Authorized"),
  NOT_AUTHORIZED("Not Authorized"),
  ERROR("error");

  public final String value;

  ApiResultStatus(String value) {
    this.value = value;
  }
}
