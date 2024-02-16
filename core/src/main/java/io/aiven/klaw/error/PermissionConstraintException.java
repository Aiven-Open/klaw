package io.aiven.klaw.error;

import jakarta.validation.ConstraintDeclarationException;

public class PermissionConstraintException extends ConstraintDeclarationException {

  public PermissionConstraintException(String msg) {
    super(msg);
  }
}
