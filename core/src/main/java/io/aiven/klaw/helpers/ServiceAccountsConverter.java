package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.ServiceAccounts;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class ServiceAccountsConverter implements DefaultAttributeConverter<ServiceAccounts> {

  @Override
  public Class<ServiceAccounts> getClassOfEntityAttribute() {
    return ServiceAccounts.class;
  }
}
