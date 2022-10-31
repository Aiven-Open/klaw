package io.aiven.klaw.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.AuthenticationRequest;
import io.aiven.klaw.model.AuthenticationResponse;
import io.aiven.klaw.service.JwtTokenUtilService;
import io.aiven.klaw.service.ValidateCaptchaService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/*
This filter authenticates the users based on the credentials provided in the request. On successful authentication, responds with a token in header.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "klaw.enable.sso", havingValue = "false")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  public static final String LOGIN_URL = "/authenticate";

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Autowired ValidateCaptchaService validateCaptchaService;

  @Autowired KwAuthenticationService kwAuthenticationService;
  @Lazy private final AuthenticationManager authManager;
  private final JwtTokenUtilService jwtTokenUtilService;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(
      AuthenticationManager authManager,
      JwtTokenUtilService jwtTokenUtilService,
      UserDetailsService userDetailsService) {
    this.authManager = authManager;
    this.jwtTokenUtilService = jwtTokenUtilService;
    this.userDetailsService = userDetailsService;
    this.setFilterProcessesUrl(LOGIN_URL);
  }

  @Override
  @Autowired
  public void setAuthenticationManager(@Lazy AuthenticationManager authenticationManager) {
    super.setAuthenticationManager(authenticationManager);
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      AuthenticationRequest req = this.getCredentials(request);

      if ("saas".equals(kwInstallationType)) {
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        boolean captchaResponse = validateCaptchaService.validateCaptcha(gRecaptchaResponse);
        if (!captchaResponse) {
          throw new AuthenticationServiceException("Invalid Captcha.");
        }
      }

      if ("ad".equals(authenticationType)) {
        // Check if user exists in kw database
        UserDetails userDetails;
        try {
          userDetails = this.userDetailsService.loadUserByUsername(req.getUsername());
          if (userDetails == null)
            return kwAuthenticationService.searchUserAttributes(request, response);
        } catch (UsernameNotFoundException e) {
          throw new RuntimeException("User/subject not found !!", e);
        }
      }

      // Authenticate user.
      return this.authManager.authenticate(
          new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication auth) {
    try {
      SecurityContextHolder.getContext().setAuthentication(auth);
      this.jwtTokenUtilService.generateToken((UserDetails) auth.getPrincipal());
      // Add token to response body
      AuthenticationResponse authenticationResponse =
          AuthenticationResponse.builder()
              .token(this.jwtTokenUtilService.generateToken((UserDetails) auth.getPrincipal()))
              .tokenType("JWT")
              .build();
      String authenticationResponseAsJson =
          new ObjectMapper().writeValueAsString(authenticationResponse);
      response.getOutputStream().write(authenticationResponseAsJson.getBytes());
      super.setAuthenticationSuccessHandler(new KwAuthenticationSuccessHandler());
      super.successfulAuthentication(request, response, chain, auth);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    super.setAuthenticationFailureHandler(new KwAuthenticationFailureHandler());
    super.unsuccessfulAuthentication(request, response, failed);
  }

  private AuthenticationRequest getCredentials(HttpServletRequest request) {
    AuthenticationRequest auth;
    try {
      auth = new ObjectMapper().readValue(request.getInputStream(), AuthenticationRequest.class);
    } catch (IOException e) {
      log.error("Error in retrieving credentials", e);
      throw new RuntimeException("Error in retrieving credentials");
    }
    return auth;
  }
}
