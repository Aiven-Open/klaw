package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.enums.RequestOperationType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseRequestModel implements Serializable {

  // CREATE / DELETE / ..
  @NotNull(message = "Request operation type must not be null")
  private RequestOperationType requestOperationType;

  @NotNull(message = "The environment must not be null")
  private String environment;

  private String appname;

  private String remarks;

  private String requestor;

  // this unique id is created after the request is made
  private Integer requestId;
}
