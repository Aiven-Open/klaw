package io.aiven.klaw.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KwAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Value("${klaw.quickstart.enabled:false}")
  private boolean quickStartEnabled;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    log.debug("User logged in : {}", authentication.getPrincipal());
    super.clearAuthenticationAttributes(request);
    response.sendRedirect(contextPath.concat(getRedirectPage(request)));
  }

  public String getRedirectPage(HttpServletRequest request) {
    DefaultSavedRequest defaultSavedRequest =
        (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    String indexPage = "index";
    String rootPath = "/";
    String providerRoute = "{{ provider }}";
    String coralTopicsUri = "/coral/topics";

    if (quickStartEnabled) {
      return coralTopicsUri;
    }

    if (defaultSavedRequest == null) {
      return indexPage;
    }

    String queryString = defaultSavedRequest.getQueryString();
    String requestUri = defaultSavedRequest.getRequestURI();

    if (requestUri != null && requestUri.contains("login")) {
      return indexPage;
    }

    if (defaultSavedRequest.getServletPath() != null
        && defaultSavedRequest.getServletPath().contains(providerRoute)) return rootPath;

    if (requestUri != null && queryString != null) {
      return requestUri.concat("?").concat(queryString);
    } else {
      return requestUri;
    }
  }
}
