package io.aiven.klaw.model.cluster;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RegisterSchemaCustomResponse implements Serializable {
  Integer id;
  Integer version;
  boolean newSchemaRegistered;
}
