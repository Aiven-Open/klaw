package io.aiven.klaw.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = KafkaClusterValidatorImpl.class)
public @interface KafkaClusterValidator {
  String message() default "Invalid kafka cluster details provided !";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
