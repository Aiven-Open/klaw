package io.aiven.klaw.clusterapi.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AivenAclStruct {
  private String id;

  private String permission;

  private String topic;

  private String username;
}
