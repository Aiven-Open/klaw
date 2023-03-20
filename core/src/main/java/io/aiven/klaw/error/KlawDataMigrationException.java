package io.aiven.klaw.error;

public class KlawDataMigrationException extends Exception {
  public KlawDataMigrationException(String error) {
    super(error);
  }
}
