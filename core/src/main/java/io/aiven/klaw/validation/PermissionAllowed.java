package io.aiven.klaw.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.aiven.klaw.model.enums.PermissionType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = PermissionAllowedValidatorImpl.class)
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface PermissionAllowed {
  PermissionType[] permissionAllowed();

  String message() default "Not Authorized";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
