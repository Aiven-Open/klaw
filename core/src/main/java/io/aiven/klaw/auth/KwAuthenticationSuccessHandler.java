package io.aiven.klaw.auth;

import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    log.info("User logged in : {}", ((UserDetails) authentication.getPrincipal()).getUsername());
    super.clearAuthenticationAttributes(request);
    if (ACTIVE_DIRECTORY.value.equals(authenticationType)) {
      response.sendRedirect(contextPath.concat(getRedirectPage(request)));
    } else response.sendRedirect(contextPath.concat(getRedirectPage(request)));
  }

  private String getRedirectPage(HttpServletRequest request) {
    DefaultSavedRequest defaultSavedRequest =
        (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    String indexPage = "index";

    if (defaultSavedRequest == null) {
      return indexPage;
    }

    String queryString = defaultSavedRequest.getQueryString();
    String requestUri = defaultSavedRequest.getRequestURI();

    if (requestUri != null && requestUri.contains("login")) {
      return indexPage;
    }

    if (requestUri != null && queryString != null) {
      return requestUri.concat("?").concat(queryString);
    } else {
      return requestUri;
    }
  }
}
