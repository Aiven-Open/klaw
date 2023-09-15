package io.aiven.klaw.helpers;

import io.aiven.klaw.model.enums.AclIPPrincipleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AclIPPrincipleTypeConverter
    implements AttributeConverter<AclIPPrincipleType, Integer> {

  @Override
  public Integer convertToDatabaseColumn(AclIPPrincipleType aclIPPrincipleType) {
    if (aclIPPrincipleType == null) {
      return null;
    }

    return aclIPPrincipleType.ordinal();
  }

  @Override
  public AclIPPrincipleType convertToEntityAttribute(Integer ordinalAclIpPrincipalType) {
    if (ordinalAclIpPrincipalType == null) {
      return null;
    }
    return switch (ordinalAclIpPrincipalType) {
      case 0 -> AclIPPrincipleType.IP_ADDRESS;
      case 1 -> AclIPPrincipleType.PRINCIPAL;
      case 2 -> AclIPPrincipleType.USERNAME;
      default -> null;
    };
  }
}
