package com.kafkamgt.uiapi.auth;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.service.ValidateCaptchaService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "kafkawize.enable.sso", havingValue = "false")
@Component
@Slf4j
public class KwRequestFilter extends UsernamePasswordAuthenticationFilter {

  @Value("${kafkawize.login.authentication.type}")
  private String authenticationType;

  @Value("${kafkawize.installation.type:onpremise}")
  private String kwInstallationType;

  @Autowired ValidateCaptchaService validateCaptchaService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired KwAuthenticationService kwAuthenticationService;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private KwAuthenticationFailureHandler kwAuthenticationFailureHandler;

  @Autowired private KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler;

  @Override
  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    super.setAuthenticationManager(authenticationManager);
  }

  // this is the starting method for authentication.
  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    if (kwInstallationType.equals("saas")) {
      String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
      boolean captchaResponse = validateCaptchaService.validateCaptcha(gRecaptchaResponse);
      if (!captchaResponse) throw new AuthenticationServiceException("Invalid Captcha.");
    }

    if (authenticationType.equals("ad")) {
      // Check if useer exists in kw database
      if (manageDatabase.getHandleDbRequests().getUsersInfo(request.getParameter("username"))
          == null) {
        return kwAuthenticationService.searchUserAttributes(request, response);
      } else {
        // User in KW db
        return super.attemptAuthentication(request, response);
      }
    } else return super.attemptAuthentication(request, response);
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
