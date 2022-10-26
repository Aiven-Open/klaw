package io.aiven.klaw.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.AuthenticationRequest;
import io.aiven.klaw.service.JwtTokenUtilService;
import io.aiven.klaw.service.jwt.util.JwtConstant;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
This filter authenticates the users based on the credentials provided in the request. On successful authentication, responds with a token in header.
 */
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  public static final String LOGIN_URL = "/authenticate";
  private final AuthenticationManager authManager;
  private final JwtTokenUtilService jwtTokenUtilService;

  public JwtAuthenticationFilter(
      AuthenticationManager authManager, JwtTokenUtilService jwtTokenUtilService) {
    this.authManager = authManager;
    this.jwtTokenUtilService = jwtTokenUtilService;
    this.setFilterProcessesUrl(LOGIN_URL);
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      AuthenticationRequest req = this.getCredentials(request);
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
      // Add token to authorization header.
      response.addHeader(
          JwtConstant.AUTHORIZATION_HEADER_STRING,
          JwtConstant.TOKEN_BEARER_PREFIX
              + this.jwtTokenUtilService.generateToken((UserDetails) auth.getPrincipal()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private AuthenticationRequest getCredentials(HttpServletRequest request) {
    AuthenticationRequest auth = null;
    try {
      auth = new ObjectMapper().readValue(request.getInputStream(), AuthenticationRequest.class);
    } catch (IOException e) {
      log.error("Error in retrieving credentials", e);
    }
    return auth;
  }
}
