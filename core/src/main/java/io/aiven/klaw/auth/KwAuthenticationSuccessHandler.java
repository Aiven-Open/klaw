package io.aiven.klaw.auth;

import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.UtilMethods;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Value("${klaw.ad.username.attribute:preferred_username}")
  private String preferredUsernameAttribute;

  @Autowired HandleDbRequestsJdbc handleDbRequests;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    log.debug("User logged in : {}", authentication.getPrincipal());
    super.clearAuthenticationAttributes(request);
    response.sendRedirect(contextPath.concat(getRedirectPage(request, authentication)));
  }

  public String getRedirectPage(HttpServletRequest request, Authentication authentication) {
    DefaultSavedRequest defaultSavedRequest =
        (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    String indexPage = "index";
    String rootPath = "/";
    String providerRoute = "{{ provider }}";
    String coralTopicsUri = "/coral/topics";

    if (quickStartEnabled
        && handleDbRequests
            .getUsersInfo(
                UtilMethods.getUserName(authentication.getPrincipal(), preferredUsernameAttribute))
            .getRole()
            .equals(KwConstants.USER_ROLE)) {
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
