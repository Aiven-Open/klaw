package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequestModel implements Serializable {

  @Size(min = 8)
  @NotNull
  String pwd;

  @Size(min = 8)
  @NotNull
  String repeatPwd;
}
