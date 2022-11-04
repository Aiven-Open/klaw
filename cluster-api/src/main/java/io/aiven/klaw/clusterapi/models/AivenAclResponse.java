package io.aiven.klaw.clusterapi.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AivenAclResponse {
  AivenAclStruct[] acl;
  String message;
}
