package io.aiven.klaw.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import lombok.Data;

@Data
public class ConnectorConfig implements Serializable {
  public String name;
  public JsonNode config;
}
