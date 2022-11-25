package io.aiven.klaw.model.enums;

/*
Ways of authenticating users
 */
public enum AuthenticationType {
  ACTIVE_DIRECTORY("ad"),
  DATABASE("db"),
  LDAP("ldap");

  public final String value;

  AuthenticationType(String value) {
    this.value = value;
  }
}
