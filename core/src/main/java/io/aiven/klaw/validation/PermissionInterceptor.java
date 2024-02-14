package io.aiven.klaw.validation;

import static io.aiven.klaw.helpers.UtilMethods.getPrincipal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.service.CommonUtilsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

  @Autowired private CommonUtilsService commonUtilsService;
  @Autowired ObjectMapper mapper;

  private static String UNAUTHORIZED_RESPONSE;

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (handler instanceof HandlerMethod) {
      PermissionAllowed checkPermission =
          ((HandlerMethod) handler).getMethodAnnotation(PermissionAllowed.class);
      if (null == checkPermission) {
        checkPermission =
            ((HandlerMethod) handler)
                .getMethod()
                .getDeclaringClass()
                .getAnnotation(PermissionAllowed.class);
      }
      // Check if you have PermissionAllowed validation annotations
      if (null != checkPermission) {

        Set<PermissionType> allowedPermissions =
            new HashSet<>(Arrays.asList(checkPermission.permissionAllowed()));
        if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), allowedPermissions)) {
          response.setStatus(HttpStatus.UNAUTHORIZED.value());
          response.getWriter().write(getUnauthorizedResponse());
          return false;
        }
        return true;
      }
    }
    return true;
  }

  private String getUnauthorizedResponse() {
    //    Initilize this just once
    if (UNAUTHORIZED_RESPONSE == null) {
      try {
        UNAUTHORIZED_RESPONSE = mapper.writeValueAsString(ApiResponse.NOT_AUTHORIZED);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    return UNAUTHORIZED_RESPONSE;
  }

  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      @Nullable ModelAndView modelAndView)
      throws Exception {}

  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      @Nullable Exception ex)
      throws Exception {}
}
