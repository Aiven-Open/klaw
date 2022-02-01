package com.kafkamgt.uiapi.model.connectorconfig;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;

@Data
public class ConnectorConfig  implements Serializable {
    public String name;
    public JsonNode config;
}
