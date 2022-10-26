package io.aiven.klaw.helpers;

import io.aiven.klaw.service.JwtTokenUtilService;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class HttpLogoutHandler implements LogoutSuccessHandler {

  private final JwtTokenUtilService jwtTokenUtilService;

  @Autowired
  public HttpLogoutHandler(JwtTokenUtilService jwtTokenUtilService) {
    this.jwtTokenUtilService = jwtTokenUtilService;
  }

  @Override
  public void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    // Invalidate token.
    String token = this.jwtTokenUtilService.extractToken(request);
    if (token != null) {
      this.jwtTokenUtilService.invalidateToken(token);
    }
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().flush();
  }
}
