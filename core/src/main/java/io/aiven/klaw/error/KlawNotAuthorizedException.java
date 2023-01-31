package io.aiven.klaw.error;

public class KlawNotAuthorizedException extends Exception {
  public KlawNotAuthorizedException(String error) {
    super(error);
  }
}
