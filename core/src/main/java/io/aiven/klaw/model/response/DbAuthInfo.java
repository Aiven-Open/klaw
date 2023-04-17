package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DbAuthInfo {

  @NotNull private String dbauth;
}
