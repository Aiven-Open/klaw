package io.aiven.klaw.model;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthenticationResponse {
  @NotNull private String token;
  @NotNull private String tokenType;
}
