package io.aiven.klaw.error;

public class KlawRestException extends Exception {
  public KlawRestException(String error) {
    super(error);
  }
}
