package io.aiven.klaw.model;

import com.sun.istack.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest implements Serializable {
  private static final long serialVersionUID = 5926202083005150707L;

  @NotNull private String username;

  @NotNull private String password;
}
