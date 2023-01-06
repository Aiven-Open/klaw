package io.aiven.klaw.validation;

import io.aiven.klaw.model.KwClustersModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaClusterValidatorImpl
    implements ConstraintValidator<KafkaClusterValidator, KwClustersModel> {
  @Override
  public boolean isValid(
      KwClustersModel KwClustersModel, ConstraintValidatorContext constraintValidatorContext) {

    return true;
    // add validations for bootstrap servers ..
  }
}
