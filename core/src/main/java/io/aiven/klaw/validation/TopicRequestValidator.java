package io.aiven.klaw.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.aiven.klaw.model.enums.PermissionType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = TopicRequestValidatorImpl.class)
public @interface TopicRequestValidator {
  String message() default "Invalid topic request details provided !";

  PermissionType getPermissionType();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
