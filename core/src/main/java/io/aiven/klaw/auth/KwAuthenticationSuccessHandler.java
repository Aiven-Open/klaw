package io.aiven.klaw.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KwAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    log.info("User logged in : {}", ((UserDetails) authentication.getPrincipal()).getUsername());
    super.clearAuthenticationAttributes(request);
  }
}
