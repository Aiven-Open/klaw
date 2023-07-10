package io.aiven.klaw.model.enums;

public enum PromotionStatusType {
  SUCCESS("success"),
  NOT_AUTHORIZED("not_authorized"),
  REQUEST_OPEN("request_open"),
  NO_PROMOTION("no_promotion"),
  FAILURE("failure");

  public final String value;

  PromotionStatusType(String value) {
    this.value = value;
  }
}
