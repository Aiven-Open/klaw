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
    // Pre existing code saves the ordinal as a string in Postgres and H2
    return String.valueOf(approvalType.ordinal());
  }

  @Override
  public ApprovalType convertToEntityAttribute(String approvalType) {
    if (approvalType == null) {
      return null;
    }
    return switch (Integer.parseInt(approvalType)) {
      case 0 -> ApprovalType.TOPIC_TEAM_OWNER;
      case 1 -> ApprovalType.CONNECTOR_TEAM_OWNER;
      case 2 -> ApprovalType.ACL_TEAM_OWNER;
      case 3 -> ApprovalType.TEAM;
      default -> null;
    };
  }
}
