package io.aiven.klaw.error;

import lombok.Data;

@Data
public class RestErrorResponse {

  private String message;

  private int error_code;
}
