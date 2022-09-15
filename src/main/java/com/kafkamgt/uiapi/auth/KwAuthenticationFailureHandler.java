package com.kafkamgt.uiapi.auth;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class KwAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      AuthenticationException e)
      throws IOException {
    httpServletResponse.sendRedirect(contextPath + "/login?error");
  }
}
