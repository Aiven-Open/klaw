package io.aiven.klaw.validation;

import static io.aiven.klaw.helpers.UtilMethods.getPrincipal;

import io.aiven.klaw.error.PermissionConstraintException;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.service.CommonUtilsService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAllowedAspectInterceptor {

  @Autowired private CommonUtilsService commonUtilsService;

  @Before("@annotation(permissionAllowed)")
  public void checkPermissions(PermissionAllowed permissionAllowed) {
    // Check if you have PermissionAllowed validation annotations
    if (null != permissionAllowed) {
      Set<PermissionType> allowedPermissions =
          new HashSet<>(Arrays.asList(permissionAllowed.permissionAllowed()));
      if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), allowedPermissions)) {
        throw new PermissionConstraintException("Not Authorized");
      }
    }
  }
}
