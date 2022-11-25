package io.aiven.klaw.model.enums;

/*
User status when being registered into Klaw
 */
public enum NewUserStatus {
  APPROVED("APPROVED"),
  STAGING("STAGING"),
  PENDING("PENDING"),
  DECLINED("DECLINED");

  public final String value;

  NewUserStatus(String value) {
    this.value = value;
  }
}
