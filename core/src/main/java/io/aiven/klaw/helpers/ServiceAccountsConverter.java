package io.aiven.klaw.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.dao.ServiceAccounts;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class ServiceAccountsConverter implements AttributeConverter<ServiceAccounts, String> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(ServiceAccounts serviceAccounts) {
    String serviceAccountsStr = null;
    try {
      if (serviceAccounts != null) {
        serviceAccountsStr = mapper.writeValueAsString(serviceAccounts);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting object to json: {}", e.getMessage());
    }
    return serviceAccountsStr;
  }

  @Override
  public ServiceAccounts convertToEntityAttribute(String serviceAccountsStr) {
    ServiceAccounts serviceAccounts = null;
    try {
      if (serviceAccountsStr != null) {
        serviceAccounts = mapper.readValue(serviceAccountsStr, ServiceAccounts.class);
      }
    } catch (JsonProcessingException e) {
      log.error("Exception converting json to object: {}", e.getMessage());
    }
    return serviceAccounts;
  }
}
