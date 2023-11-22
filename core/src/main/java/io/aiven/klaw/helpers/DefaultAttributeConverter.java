package io.aiven.klaw.helpers;

import static io.aiven.klaw.service.UsersTeamsControllerService.OBJECT_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DefaultAttributeConverter<X> extends AttributeConverter<X, String> {

  Logger LOG = LoggerFactory.getLogger(DefaultAttributeConverter.class);

  @Override
  default String convertToDatabaseColumn(X x) {
    return convertToDatabaseColumn(x, null);
  }

  default String convertToDatabaseColumn(X x, String defaultValue) {
    String res = defaultValue;
    try {
      if (x != null) {
        res = OBJECT_MAPPER.writeValueAsString(x);
      }
    } catch (JsonProcessingException e) {
      LOG.error("Exception converting object to json: {}", e.getMessage());
    }
    return res;
  }

  @Override
  default X convertToEntityAttribute(String value) {
    return convertToEntityAttribute(value, null);
  }

  default X convertToEntityAttribute(String value, X defaultValue) {
    X res = defaultValue;
    try {
      if (value != null) {
        Class<X> classOfEntityAttribute = getClassOfEntityAttribute();
        if (classOfEntityAttribute != null) {
          res = OBJECT_MAPPER.readValue(value, classOfEntityAttribute);
        } else {
          TypeReference<X> typeReference = getTypeReference();
          if (typeReference != null) {
            res = OBJECT_MAPPER.readValue(value, typeReference);
          } else {
            throw new RuntimeException(
                "Either getClassOfEntityAttribute or getTypeReference should provide non null value");
          }
        }
      }
    } catch (JsonProcessingException e) {
      LOG.error("Exception converting json to object: {}", e.getMessage());
    }
    return res;
  }

  default Class<X> getClassOfEntityAttribute() {
    return null;
  }

  default TypeReference<X> getTypeReference() {
    return null;
  }
}
