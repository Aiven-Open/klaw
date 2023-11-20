package io.aiven.klaw.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.Map;

@Converter
public class AivenAclIdConverter implements DefaultAttributeConverter<Map<String, String>> {

  private static final TypeReference<Map<String, String>> TYPE_REFERENCE = new TypeReference<>() {};

  @Override
  public String convertToDatabaseColumn(Map<String, String> aclIdMap) {
    return convertToDatabaseColumn(aclIdMap, "");
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String aclIdMapJson) {
    return convertToEntityAttribute(aclIdMapJson, Collections.emptyMap());
  }

  @Override
  public TypeReference<Map<String, String>> getTypeReference() {
    return TYPE_REFERENCE;
  }
}
