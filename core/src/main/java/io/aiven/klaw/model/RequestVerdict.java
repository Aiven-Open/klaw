package io.aiven.klaw.model;

import io.aiven.klaw.model.enums.ResourceType;
import lombok.Data;

@Data
public class RequestVerdict {

  // Reason for declining a request only required on declining Requests
  private String reason;

  private ResourceType resourceType;

  private String reqId;
}
