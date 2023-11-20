package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.EnvTag;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class EnvTagConverter implements DefaultAttributeConverter<EnvTag> {

  @Override
  public Class<EnvTag> getClassOfEntityAttribute() {
    return EnvTag.class;
  }
}
