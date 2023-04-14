package io.aiven.klaw.clusterapi.models;

import lombok.Data;

@Data
public class ServiceAccountDetails {
  private String username;

  private String password;

  private boolean accountFound;
}
