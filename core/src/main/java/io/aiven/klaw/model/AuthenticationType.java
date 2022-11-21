package io.aiven.klaw.model;

/*
Ways of authenticating users
 */
public enum AuthenticationType {
  ACTIVE_DIRECTORY("ad"),
  AZURE_ACTIVE_DIRECTORY("azuread"),
  DATABASE("db"),
  LDAP("ldap");

  public final String value;

  AuthenticationType(String value) {
    this.value = value;
  }
}
