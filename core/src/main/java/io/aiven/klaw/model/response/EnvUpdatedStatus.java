package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class EnvUpdatedStatus {
  private String result;

  private String envstatus;
}
