package io.aiven.klaw.model;

import lombok.Data;

@Data
public class SSODetails {
  private String imageURI;
  private String ssoProvider;
  private String url;
}
