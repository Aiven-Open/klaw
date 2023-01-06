package io.aiven.klaw.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
