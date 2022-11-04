package io.aiven.klaw.error;

public class KlawException extends Exception {
  public KlawException(String error) {
    super(error);
  }
}
