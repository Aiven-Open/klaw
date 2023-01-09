package io.aiven.klaw.auth;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.service.ValidateCaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;

@ConditionalOnProperty(name = "klaw.enable.sso", havingValue = "false")
@Slf4j
public class KwRequestFilter extends UsernamePasswordAuthenticationFilter {

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Autowired ValidateCaptchaService validateCaptchaService;

  ManageDatabase manageDatabase;

  @Autowired KwAuthenticationService kwAuthenticationService;

  private AuthenticationManager authenticationManager;

  private KwAuthenticationFailureHandler kwAuthenticationFailureHandler;

  private KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler;

  public KwRequestFilter(AuthenticationManager authenticationManager, ManageDatabase manageDatabase, KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler, KwAuthenticationFailureHandler kwAuthenticationFailureHandler) {
    this.authenticationManager = authenticationManager;
    this.manageDatabase = manageDatabase;
    this.kwAuthenticationSuccessHandler = kwAuthenticationSuccessHandler;
    this.kwAuthenticationFailureHandler = kwAuthenticationFailureHandler;
    super.setAuthenticationManager(this.authenticationManager);
  }

  @Override
//  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    super.setAuthenticationManager(this.authenticationManager);
  }

  // this is the starting method for authentication.
  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    if ("saas".equals(kwInstallationType)) {
      String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
      boolean captchaResponse = validateCaptchaService.validateCaptcha(gRecaptchaResponse);
      if (!captchaResponse) {
        throw new AuthenticationServiceException("Invalid Captcha.");
      }
    }

    if (ACTIVE_DIRECTORY.value.equals(authenticationType)) {
      // Check if user exists in kw database
      if (manageDatabase.getHandleDbRequests().getUsersInfo(request.getParameter("username"))
          == null) {
        return kwAuthenticationService.searchUserAttributes(request, response);
      } else {
        // User in KW db
        return super.attemptAuthentication(request, response);
      }
    } else {
      return super.attemptAuthentication(request, response);
    }
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    super.setAuthenticationFailureHandler(kwAuthenticationFailureHandler);
    super.unsuccessfulAuthentication(request, response, failed);
  }

  @Override
  public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
    super.setAuthenticationFailureHandler(kwAuthenticationFailureHandler);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult)
      throws IOException, ServletException {
    super.setAuthenticationSuccessHandler(kwAuthenticationSuccessHandler);
    super.successfulAuthentication(request, response, chain, authResult);
  }
}
