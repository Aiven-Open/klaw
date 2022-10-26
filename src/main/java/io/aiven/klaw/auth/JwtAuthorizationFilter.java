package io.aiven.klaw.auth;

import io.aiven.klaw.service.JwtTokenUtilService;
import io.aiven.klaw.service.jwt.util.JwtConstant;
import io.jsonwebtoken.Claims;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/*
This filter authorizes the incoming requests based on token in headers
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

  private final UserDetailsService userDetailsService;
  private final JwtTokenUtilService jwtTokenUtilService;

  public JwtAuthorizationFilter(
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      JwtTokenUtilService jwtTokenUtilService) {
    super(authenticationManager);
    this.userDetailsService = userDetailsService;
    this.jwtTokenUtilService = jwtTokenUtilService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
    try {
      // Check for authorization header existence.
      String header = request.getHeader(JwtConstant.AUTHORIZATION_HEADER_STRING);
      if (header == null || !header.startsWith(JwtConstant.TOKEN_BEARER_PREFIX)) {
        chain.doFilter(request, response);
        return;
      }
      // Validate request..
      UsernamePasswordAuthenticationToken authorization = authorizeRequest(request);
      SecurityContextHolder.getContext().setAuthentication(authorization);
      chain.doFilter(request, response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Validate token.
   *
   * @param request
   * @return authentication
   */
  private UsernamePasswordAuthenticationToken authorizeRequest(HttpServletRequest request) {
    try {
      // Get token.
      String token = this.jwtTokenUtilService.extractToken(request);
      if (token != null) {
        // Validate token.
        Claims claims = this.jwtTokenUtilService.getAllClaimsFromToken(token);
        // Validate user authority/role if allowed to do the api dto.
        String user = claims.getSubject();
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(user);
        if (userDetails != null) {
          return new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return null;
  }
}
