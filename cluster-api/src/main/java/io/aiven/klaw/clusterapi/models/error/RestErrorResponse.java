package io.aiven.klaw.clusterapi.models.error;

import lombok.Data;

@Data
public class RestErrorResponse {

  private String message;

  private int error_code;
}
