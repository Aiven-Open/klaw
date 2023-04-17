package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class AclsCountPerEnv {
  private String status;

  private String aclsCount;
}
