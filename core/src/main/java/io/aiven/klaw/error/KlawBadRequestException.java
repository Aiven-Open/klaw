package io.aiven.klaw.error;

public class KlawBadRequestException extends Exception {
  public KlawBadRequestException(String error) {
    super(error);
  }
}
