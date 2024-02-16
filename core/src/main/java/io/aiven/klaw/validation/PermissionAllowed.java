package io.aiven.klaw.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.aiven.klaw.model.enums.PermissionType;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RUNTIME)
@Inherited
@Order(Ordered.HIGHEST_PRECEDENCE)
public @interface PermissionAllowed {
  PermissionType[] permissionAllowed();

  String message() default "Not Authorized";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
