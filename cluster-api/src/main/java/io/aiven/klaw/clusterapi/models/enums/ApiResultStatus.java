package io.aiven.klaw.clusterapi.models.enums;

public enum ApiResultStatus {
  SUCCESS("success"),
  PARTIAL("partial"),
  ERROR("error"),
  FAILURE("failure");

  public final String value;

  private ApiResultStatus(String value) {
    this.value = value;
  }
}
