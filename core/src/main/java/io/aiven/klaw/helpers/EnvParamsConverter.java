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
  public String convertToDatabaseColumn(EnvParams EnvParams) {
    String EnvParamsStr = null;
    try {
      if (EnvParams != null) {
        EnvParamsStr = mapper.writeValueAsString(EnvParams);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting object to json: {}", e.getMessage());
    }
    return EnvParamsStr;
  }

  @Override
  public EnvParams convertToEntityAttribute(String EnvParamsStr) {
    EnvParams tag = null;
    try {
      if (EnvParamsStr != null) {
        tag = mapper.readValue(EnvParamsStr, EnvParams.class);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting json to object: {}", e.getMessage());
    }
    return tag;
  }
}
