package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.enums.OperationalRequestType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseOperationalRequestModel implements Serializable {

  @NotNull private OperationalRequestType operationalRequestType;

  @NotNull private String environment;

  @NotNull private String description;

  private Integer requestingTeamId;

  private String approvingTeamId;

  private String otherParams;

  private String appname;

  private String remarks;

  private String requestor;

  // this unique id is created after the request is made
  private Integer requestId;
}
