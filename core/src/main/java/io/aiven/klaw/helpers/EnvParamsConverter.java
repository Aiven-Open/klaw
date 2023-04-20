package io.aiven.klaw.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.response.EnvParams;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class EnvParamsConverter implements AttributeConverter<EnvParams, String> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(EnvParams envParams) {
    String envParamsStr = null;
    try {
      if (envParams != null) {
        envParamsStr = mapper.writeValueAsString(envParams);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting object: {} to json: {}", envParams, e.getMessage());
    }
    return envParamsStr;
  }

  @Override
  public EnvParams convertToEntityAttribute(String envParamsStr) {
    EnvParams params = null;
    try {
      if (envParamsStr != null) {
        params = mapper.readValue(envParamsStr, EnvParams.class);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting json: {} to object: {}", envParamsStr, e.getMessage());
    }
    return params;
  }
}
