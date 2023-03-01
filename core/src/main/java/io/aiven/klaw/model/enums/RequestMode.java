package io.aiven.klaw.model.enums;

public enum RequestMode {
  TO_APPROVE("to_approve"),
  MY_APPROVALS("my_approvals"),
  MY_REQUESTS("my_requests");

  public final String value;

  RequestMode(String value) {
    this.value = value;
  }
}
