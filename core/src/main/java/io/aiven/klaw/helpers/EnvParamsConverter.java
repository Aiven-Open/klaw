package io.aiven.klaw.helpers;

import io.aiven.klaw.model.response.EnvParams;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class EnvParamsConverter implements DefaultAttributeConverter<EnvParams> {

  @Override
  public Class<EnvParams> getClassOfEntityAttribute() {
    return EnvParams.class;
  }
}
