package io.aiven.klaw.error;

public class KlawValidationException extends Exception {
  public KlawValidationException(String error) {
    super(error);
  }
}
