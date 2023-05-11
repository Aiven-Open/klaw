package io.aiven.klaw.helpers;

import static io.aiven.klaw.service.UsersTeamsControllerService.OBJECT_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

@Converter
public class AivenAclIdConverter implements AttributeConverter<Map<String, String>, String> {

  @Override
  public String convertToDatabaseColumn(Map<String, String> aclIdMap) {
    try {
      if (aclIdMap != null) return OBJECT_MAPPER.writer().writeValueAsString(aclIdMap);
      else return "";
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String aclIdMapJson) {
    try {
      if (aclIdMapJson != null)
        return OBJECT_MAPPER.readValue(aclIdMapJson, new TypeReference<>() {});
      else return new HashMap<>();
    } catch (JsonProcessingException e) {
      return new HashMap<>();
    }
  }
}
