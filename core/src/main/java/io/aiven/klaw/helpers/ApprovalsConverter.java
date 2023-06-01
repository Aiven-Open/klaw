package io.aiven.klaw.helpers;

import static io.aiven.klaw.service.UsersTeamsControllerService.OBJECT_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.aiven.klaw.model.Approval;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class ApprovalsConverter implements AttributeConverter<List<Approval>, String> {

  @Override
  public String convertToDatabaseColumn(List<Approval> approvalList) {
    try {
      if (approvalList != null) return OBJECT_MAPPER.writer().writeValueAsString(approvalList);
      else return "";
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  @Override
  public List<Approval> convertToEntityAttribute(String approvalListStr) {
    try {
      if (approvalListStr != null) {
        return OBJECT_MAPPER.readValue(approvalListStr, new TypeReference<>() {});
      } else {
        return new ArrayList<>();
      }
    } catch (JsonProcessingException e) {
      return new ArrayList<>();
    }
  }
}
