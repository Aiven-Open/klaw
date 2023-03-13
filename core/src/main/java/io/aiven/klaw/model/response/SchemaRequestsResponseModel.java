package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemaRequestsResponseModel extends BaseRequestsResponseModel implements Serializable {

  @NotNull
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

  @NotNull
  @Size(min = 8, message = "Please fill in a valid schema.")
  private String schemafull;

  private Integer req_no;

  private String schemaversion;

  private Boolean forceRegister;
}
