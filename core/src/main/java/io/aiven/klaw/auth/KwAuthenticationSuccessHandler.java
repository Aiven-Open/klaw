package io.aiven.klaw.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KwAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws ServletException, IOException {
    log.info("User logged in : {}", ((UserDetails) authentication.getPrincipal()).getUsername());
    String getRedirectPage = validateUrl(request.getParameter("urlbar"));
    super.clearAuthenticationAttributes(request);
    response.sendRedirect(getRedirectPage);
  }

  private String validateUrl(String urlFromAddressBar) {
    if (urlFromAddressBar != null) {
      urlFromAddressBar = urlFromAddressBar.replace(contextPath + "/", "");

      if (urlFromAddressBar.contains("login")) {
        urlFromAddressBar = "index";
      }

      if (!urlFromAddressBar.contains("loggedin=true")) {
        if (urlFromAddressBar.contains("?")) {
          urlFromAddressBar += "&loggedin=true";
        } else {
          urlFromAddressBar += "?loggedin=true";
        }
      }

    } else {
      urlFromAddressBar = "";
    }

    return urlFromAddressBar;
  }
}
