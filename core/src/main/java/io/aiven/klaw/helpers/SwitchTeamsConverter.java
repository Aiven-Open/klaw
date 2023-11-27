package io.aiven.klaw.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.Set;

@Converter
public class SwitchTeamsConverter implements DefaultAttributeConverter<Set<Integer>> {

  private static final TypeReference<Set<Integer>> TYPE_REFERENCE = new TypeReference<>() {};

  @Override
  public String convertToDatabaseColumn(Set<Integer> switchTeamsList) {
    return convertToDatabaseColumn(switchTeamsList, "");
  }

  @Override
  public Set<Integer> convertToEntityAttribute(String switchTeamsJson) {
    return convertToEntityAttribute(switchTeamsJson, Collections.emptySet());
  }

  @Override
  public TypeReference<Set<Integer>> getTypeReference() {
    return TYPE_REFERENCE;
  }
}
