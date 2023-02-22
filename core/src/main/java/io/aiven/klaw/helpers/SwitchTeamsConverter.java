package io.aiven.klaw.helpers;

import static io.aiven.klaw.service.UsersTeamsControllerService.OBJECT_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashSet;
import java.util.Set;

@Converter
public class SwitchTeamsConverter implements AttributeConverter<Set<Integer>, String> {

  @Override
  public String convertToDatabaseColumn(Set<Integer> switchTeamsList) {
    try {
      if (switchTeamsList != null)
        return OBJECT_MAPPER.writer().writeValueAsString(switchTeamsList);
      else return "";
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  @Override
  public Set<Integer> convertToEntityAttribute(String switchTeamsJson) {
    try {
      if (switchTeamsJson != null)
        return OBJECT_MAPPER.readValue(switchTeamsJson, new TypeReference<>() {});
      else return new HashSet<>();
    } catch (JsonProcessingException e) {
      return new HashSet<>();
    }
  }
}
