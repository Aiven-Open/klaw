package io.aiven.klaw.clusterapi.config;

import io.aiven.klaw.clusterapi.services.JwtTokenUtilService;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

  public static final String USERNAME = "username";
  public static final String TOKEN = "token";
  private final JwtTokenUtilService jwtTokenUtil;
  private final UserDetailsService userDetailsService;

  public JwtRequestFilter(
      UserDetailsService userDetailsService, JwtTokenUtilService jwtTokenUtilService) {
    this.userDetailsService = userDetailsService;
    this.jwtTokenUtil = jwtTokenUtilService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    final Map<String, String> userTokenMap = new HashMap<>();
    if (isUnAuthorizedRequest(request, response, userTokenMap)) return;

    validateToken(request, response, chain, userTokenMap);
  }

  private boolean isUnAuthorizedRequest(
      HttpServletRequest request, HttpServletResponse response, Map<String, String> userTokenMap)
      throws IOException {
    final String requestTokenHeader = request.getHeader("Authorization");
    String jwtToken;
    final String bearer = "Bearer ";
    // JWT Token is in the form "Bearer token". Remove Bearer word and get
    // only the Token
    if (requestTokenHeader != null && requestTokenHeader.startsWith(bearer)) {
      jwtToken = requestTokenHeader.substring(bearer.length());
      userTokenMap.put(TOKEN, jwtToken);
      return extractUsernameFromToken(response, userTokenMap, jwtToken);
    } else {
      if (requestTokenHeader != null) {
        log.warn(
            "Invalid JWT : {}",
            requestTokenHeader.substring(0, Math.min(requestTokenHeader.length(), 10)));
      }

      log.warn("JWT Token does not begin with Bearer String");
    }
    return false;
  }

  private boolean extractUsernameFromToken(
      HttpServletResponse response, Map<String, String> userTokenMap, String jwtToken)
      throws IOException {
    String username;
    try {
      username = jwtTokenUtil.getUsernameFromToken(jwtToken);
      userTokenMap.put(USERNAME, username);
    } catch (IllegalArgumentException e) {
      log.info("Unable to get JWT Token ", e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return true;
    } catch (ExpiredJwtException e) {
      log.info("JWT Token has expired", e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return true;
    } catch (Exception e) {
      log.info("Token validation errors ", e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return true;
    }
    return false;
  }

  // Once we get the token validate it.
  private void validateToken(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Map<String, String> userTokenMap)
      throws IOException, ServletException {
    if (userTokenMap.containsKey(USERNAME)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails;
      try {
        userDetails = this.userDetailsService.loadUserByUsername(userTokenMap.get(USERNAME));
      } catch (UsernameNotFoundException e) {
        throw new RuntimeException("User/subject not found !!", e);
      }
      validateTokenAndUserDetails(request, response, userTokenMap, userDetails);
    }
    chain.doFilter(request, response);
  }

  private void validateTokenAndUserDetails(
      HttpServletRequest request,
      HttpServletResponse response,
      Map<String, String> userTokenMap,
      UserDetails userDetails)
      throws IOException {
    // authentication
    if (userDetails != null
        && userTokenMap.containsKey(TOKEN)
        && jwtTokenUtil.validateToken(userTokenMap.get(TOKEN))) {
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      usernamePasswordAuthenticationToken.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request));
      // After setting the Authentication in the context, we specify
      // that the current user is authenticated. So it passes the
      // Spring Security Configurations successfully.
      SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    } else {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
  }
}
