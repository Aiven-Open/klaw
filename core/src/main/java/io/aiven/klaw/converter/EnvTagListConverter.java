package io.aiven.klaw.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.dao.EnvTag;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class EnvTagListConverter implements AttributeConverter<List<EnvTag>, String> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<EnvTag> envTags) {
    String envTagsStr = null;
    try {
      envTagsStr = mapper.writeValueAsString(envTags);
    } catch (JsonProcessingException e) {
      log.error("Exception converting object to json: {}", e.getMessage());
    }
    return envTagsStr;
  }

  @Override
  public List<EnvTag> convertToEntityAttribute(String envTagsStr) {
    List<EnvTag> envTags = null;
    try {
      envTags = mapper.readValue(envTagsStr, new TypeReference<List<EnvTag>>() {});
    } catch (JsonProcessingException e) {
      log.error("Exception converting json to object: {}", e.getMessage());
    }
    return envTags;
  }
}
