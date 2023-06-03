package io.aiven.klaw.clusterapi.models;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RegisterSchemaCustomResponse implements Serializable {
  private Integer id;
  private Integer version;
  private boolean schemaRegistered;
  private String compatibility;
}
