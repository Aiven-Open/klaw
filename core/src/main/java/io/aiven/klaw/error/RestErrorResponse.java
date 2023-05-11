package io.aiven.klaw.error;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RestErrorResponse {

  private String message;

  @JsonAlias({"errorCode", "error_code"})
  private int errorCode;
}
