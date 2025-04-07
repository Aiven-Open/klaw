package io.aiven.klaw.validation;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX_VALIDATION_STR;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidatorImpl.class)
public @interface PasswordValidator {
  String message() default PASSWORD_REGEX_VALIDATION_STR;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
