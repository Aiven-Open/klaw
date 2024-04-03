package io.aiven.klaw.helpers;

import io.aiven.klaw.model.enums.ApprovalType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ApprovalTypeConverter implements AttributeConverter<ApprovalType, String> {

  @Override
  public String convertToDatabaseColumn(ApprovalType approvalType) {
    if (approvalType == null) {
      return null;
    }

    return approvalType.name();
  }

  @Override
  public ApprovalType convertToEntityAttribute(String approvalType) {
    if (approvalType == null) {
      return null;
    }
    return ApprovalType.of(approvalType);
  }
}
