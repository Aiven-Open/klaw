package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KafkaConnectorRequestModel extends BaseRequestModel implements Serializable {

  @NotNull(message = "Connector name must not be null")
  @Pattern(message = "Invalid connector name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String connectorName;

  @NotNull(message = "Connector configuration must not be null")
  private String connectorConfig;

  @NotNull(message = "Connector description must not be null")
  @Pattern(message = "Invalid description", regexp = "^[a-zA-Z 0-9_.,-]{3,}$")
  @Size(
      min = 1,
      max = 100,
      message =
          "Description must be a minimum of 1 character and a maximum of 100, this can be less if multibyte encoding is being used.")
  private String description;

  private Integer teamId;

  private String otherParams;
}
