package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.SchemaType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemaRequestsResponseModel extends BaseRequestsResponseModel implements Serializable {

  @NotNull private String topicname;

  @NotNull private String schemafull;

  @NotNull private Integer req_no;

  @NotNull private Boolean forceRegister;

  private SchemaType schemaType;

  private String schemaversion;
}
