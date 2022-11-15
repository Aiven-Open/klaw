package io.aiven.klaw.validation;

import io.aiven.klaw.model.TopicRequestModel;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicRequestValidatorImpl
    implements ConstraintValidator<TopicRequestValidator, TopicRequestModel> {

  @Override
  public void initialize(TopicRequestValidator constraintAnnotation) {}

  @Override
  public boolean isValid(
      TopicRequestModel topicRequestModel, ConstraintValidatorContext constraintValidatorContext) {
    return true;
    // TODO move logic from service layer to here for validating topic request
  }
}
