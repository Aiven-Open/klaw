package io.aiven.klaw.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.dao.EnvTag;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class EnvTagConverter implements AttributeConverter<EnvTag, String> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(EnvTag envTag) {
    String envTagStr = null;
    try {
      if (envTag != null) {
        envTagStr = mapper.writeValueAsString(envTag);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting object to json: {}", e.getMessage());
    }
    return envTagStr;
  }

  @Override
  public EnvTag convertToEntityAttribute(String envTagStr) {
    EnvTag tag = null;
    try {
      if (envTagStr != null) {
        tag = mapper.readValue(envTagStr, EnvTag.class);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting json to object: {}", e.getMessage());
    }
    return tag;
  }
}
