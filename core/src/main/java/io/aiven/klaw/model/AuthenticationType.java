package io.aiven.klaw.model;

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
