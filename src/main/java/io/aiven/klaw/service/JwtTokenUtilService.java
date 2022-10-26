package io.aiven.klaw.service;

import io.aiven.klaw.service.jwt.util.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.Serializable;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import static io.aiven.klaw.service.jwt.util.JwtConstant.ACCESS_TOKEN_EXPIRATION;

@Service
public class JwtTokenUtilService implements Serializable {
  private static final long serialVersionUID = -2020300565626007488L;

  @Value("${klaw.access.base64.secret}")
  private String secret;

  // for retrieving any information from token we will need the secret key
  public Claims getAllClaimsFromToken(String token) {
    Key hmacKey =
        new SecretKeySpec(Base64.decodeBase64(secret), SignatureAlgorithm.HS256.getJcaName());
    Jws<Claims> jwt = Jwts.parserBuilder().setSigningKey(hmacKey).build().parseClaimsJws(token);
    return jwt.getBody();
  }

  public String generateToken(UserDetails userDetails) {
    return doGenerateToken(userDetails.getUsername());
  }

  private String doGenerateToken(String username) {
    Key hmacKey =
        new SecretKeySpec(Base64.decodeBase64(secret), SignatureAlgorithm.HS256.getJcaName());
    Instant now = Instant.now();

    return Jwts.builder()
        .claim("name", username)
        .setSubject(username)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(ACCESS_TOKEN_EXPIRATION, ChronoUnit.MINUTES)))
        .signWith(hmacKey)
        .compact();
  }

  public void invalidateToken(String token) {
    final Claims claims = getAllClaimsFromToken(token);
    claims.setExpiration(new Date());
  }

  public String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader(JwtConstant.AUTHORIZATION_HEADER_STRING);
    if (authHeader != null && authHeader.startsWith(JwtConstant.TOKEN_BEARER_PREFIX)) {
      return authHeader.replace(JwtConstant.TOKEN_BEARER_PREFIX, "");
    }
    return null;
  }
}
