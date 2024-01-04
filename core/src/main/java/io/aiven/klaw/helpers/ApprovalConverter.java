package io.aiven.klaw.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import io.aiven.klaw.dao.Approval;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.List;

@Converter
public class ApprovalConverter implements DefaultAttributeConverter<List<Approval>> {

  private static final TypeReference<List<Approval>> TYPE_REFERENCE = new TypeReference<>() {};

  @Override
  public String convertToDatabaseColumn(List<Approval> approvals) {
    return convertToDatabaseColumn(approvals, null);
  }

  @Override
  public List<Approval> convertToEntityAttribute(String approvalListJson) {
    return convertToEntityAttribute(approvalListJson, Collections.emptyList());
  }

  @Override
  public TypeReference<List<Approval>> getTypeReference() {
    return TYPE_REFERENCE;
  }
}
