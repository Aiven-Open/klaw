package io.aiven.klaw.clusterapi.models.error;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RestErrorResponse {

  private String message;

  @JsonAlias("error_code")
  private int errorCode;
}
