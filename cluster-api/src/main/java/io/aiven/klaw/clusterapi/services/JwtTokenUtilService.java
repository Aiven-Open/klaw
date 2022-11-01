package io.aiven.klaw.clusterapi.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenUtilService implements InitializingBean {
  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterApiSecret;

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
  private Claims getAllClaimsFromToken(String token) {
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

  // validate secret during app initialization
  @Override
  public void afterPropertiesSet() throws Exception {
    if (clusterApiSecret != null && !clusterApiSecret.trim().equals("")) {
      try {
        decodedSecret = Base64.decodeBase64(clusterApiSecret.getBytes());
        return;
      } catch (Exception e) {
        throw new Exception(
            "Invalid Base64 value configured. klaw.clusterapi.access.base64.secret");
      }
    }
    throw new Exception("Property not configured. klaw.clusterapi.access.base64.secret");
  }
}
