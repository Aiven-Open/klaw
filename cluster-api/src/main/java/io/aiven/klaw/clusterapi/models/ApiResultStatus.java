package io.aiven.klaw.clusterapi.models;

public enum ApiResultStatus {
  SUCCESS("success"),
  ERROR("error"),
  FAILURE("failure");

  public final String value;

  private ApiResultStatus(String value) {
    this.value = value;
  }
}
