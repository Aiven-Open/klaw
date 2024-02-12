package io.aiven.klaw.validation;

import static io.aiven.klaw.helpers.UtilMethods.getPrincipal;

import io.aiven.klaw.error.PermissionConstraintException;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.service.CommonUtilsService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PermissionAllowedValidatorImpl
    implements ConstraintValidator<PermissionAllowed, Object> {
  private PermissionType[] permissions;
  @Autowired private CommonUtilsService commonUtilsService;

  @Override
  public void initialize(PermissionAllowed constraintAnnotation) {
    this.permissions = constraintAnnotation.permissionAllowed();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
    log.debug("Checking for permission(s) {}", permissions);
    Set<PermissionType> allowedPermissions = new HashSet<>(Arrays.asList(permissions));
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), allowedPermissions)) {
      throw new PermissionConstraintException("Not Authorized");
    }
    return true;
  }
}
