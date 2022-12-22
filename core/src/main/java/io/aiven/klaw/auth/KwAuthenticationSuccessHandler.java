package io.aiven.klaw.auth;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KwAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    log.info("User logged in : {}", ((UserDetails) authentication.getPrincipal()).getUsername());
    super.clearAuthenticationAttributes(request);
    response.sendRedirect(contextPath.concat(getRedirectPage(request)));
  }

  private String getRedirectPage(HttpServletRequest request) {
    DefaultSavedRequest defaultSavedRequest =
        (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    String loggedInQuery = "loggedin=true";
    String indexPage = "index";
    String urlSeparator = "?";
    String defaultPage = indexPage.concat(urlSeparator).concat(loggedInQuery);

    if (defaultSavedRequest == null) {
      return defaultPage;
    }

    String queryString = defaultSavedRequest.getQueryString();
    String requestUri = defaultSavedRequest.getRequestURI();

    if (requestUri != null && requestUri.contains("login")) {
      return defaultPage;
    }

    if (queryString != null && queryString.contains(loggedInQuery)) {
      return indexPage.concat(urlSeparator).concat(queryString);
    }

    if (requestUri != null && queryString != null) {
      return requestUri.concat(urlSeparator).concat(queryString);
    } else {
      return requestUri;
    }
  }
}
