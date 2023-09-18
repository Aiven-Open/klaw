package io.aiven.klaw.error;

import org.springframework.security.core.AuthenticationException;

public class ActiveDirectoryAuthenticationException extends AuthenticationException {

  public ActiveDirectoryAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
