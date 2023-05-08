package io.aiven.klaw.clusterapi.models;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterSchemaResponse implements Serializable {
  Integer id;
}
