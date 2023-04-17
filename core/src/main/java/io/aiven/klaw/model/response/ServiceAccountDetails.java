package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class ServiceAccountDetails {
  private String username;

  private String password;

  private boolean accountFound;
}
