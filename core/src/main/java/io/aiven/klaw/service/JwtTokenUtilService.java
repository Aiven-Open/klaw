package io.aiven.klaw.service;

import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "klaw.core.ha", name = "enable")
public class JwtTokenUtilService implements InitializingBean {
  @Value("${klaw.core.app2app.base64.secret:#{''}}")
  private String app2AppSecret;

  public static final String BEARER = "Bearer ";
  @Autowired UserDetailsService userDetailsService;
  private static byte[] decodedSecret;

  // retrieve username from jwt token
  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  // retrieve expiration date from jwt token
  private Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  // for retrieving any information from token we will need the secret key
  public Claims getAllClaimsFromToken(String token) {
    Key hmacKey = new SecretKeySpec(decodedSecret, SignatureAlgorithm.HS256.getJcaName());
    Jws<Claims> jwt = Jwts.parserBuilder().setSigningKey(hmacKey).build().parseClaimsJws(token);
    return jwt.getBody();
  }

  // check if the token has expired
  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  // validate token
  public Boolean validateToken(String token) {
    return !isTokenExpired(token);
  }

  public void validateRole(String token, String... expectedRole) throws KlawNotAuthorizedException {

    token = token.substring(BEARER.length()).trim();
    Claims claims = getAllClaimsFromToken(token);
    UserDetails user = userDetailsService.loadUserByUsername(getUsernameFromToken(token));
    if (user == null
        && !validateToken(token)
        && !claims.get("Roles", List.class).containsAll(List.<String>of(expectedRole))) {
      throw new KlawNotAuthorizedException("UnAuthorized");
    }
  }

  // validate secret during app initialization
  @Override
  public void afterPropertiesSet() throws Exception {
    if (app2AppSecret != null && !app2AppSecret.trim().isEmpty()) {
      try {
        decodedSecret = Base64.decodeBase64(app2AppSecret);
        return;
      } catch (Exception e) {
        throw new Exception("Invalid Base64 value configured. klaw.core.app2app.base64.secret");
      }
    }
    throw new Exception("Property not configured. klaw.core.app2app.base64.secret");
  }
}
