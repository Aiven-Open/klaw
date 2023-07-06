package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.util.codec.binary.Base64;
import org.assertj.core.api.AbstractThrowableAssert;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenUtilServiceTest {
  private final String clusterApiSecret = "dGhpcyBpcyBhIHNlY3JldCB0byBhY2Nlc3MgY2x1c3RlcmFwaQ==";
  private final byte[] decodedSecret = Base64.decodeBase64(clusterApiSecret);
  JwtTokenUtilService jwtTokenUtilService;

  @BeforeEach
  public void setUp() {
    jwtTokenUtilService = new JwtTokenUtilService();
    ReflectionTestUtils.setField(jwtTokenUtilService, "clusterApiSecret", clusterApiSecret);
    ReflectionTestUtils.setField(jwtTokenUtilService, "decodedSecret", decodedSecret);
  }

  @Test
  void getUsernameFromToken() {
    SecretKey hmacKey = new SecretKeySpec(decodedSecret, SignatureAlgorithm.HS256.getJcaName());
    String username = "user123";
    String token =
        Jwts.builder()
            .setSubject(username)
            .claim("name", "John Doe")
            .claim("role", "admin")
            .signWith(hmacKey)
            .compact();

    String actual = jwtTokenUtilService.getUsernameFromToken(token);

    assertThat(actual).isEqualTo(username);
  }

  @Test
  void validateToken() {
    SecretKey hmacKey = new SecretKeySpec(decodedSecret, SignatureAlgorithm.HS256.getJcaName());
    String username = "user123";
    String token =
        Jwts.builder()
            .setSubject(username)
            .setExpiration(new DateTime().plusDays(1).toDate())
            .claim("name", "John Doe")
            .claim("role", "admin")
            .signWith(hmacKey)
            .compact();

    Boolean actual = jwtTokenUtilService.validateToken(token);

    assertThat(actual).isTrue();
  }

  @Test
  void afterPropertiesSet_PropertyNotConfigured() {
    ReflectionTestUtils.setField(jwtTokenUtilService, "clusterApiSecret", "");

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> jwtTokenUtilService.afterPropertiesSet());
    exception.isInstanceOf(Exception.class);
    exception.hasMessage("Property not configured. klaw.clusterapi.access.base64.secret");
  }

  @Test
  void afterPropertiesSet_InvalidBase64Value() {
    ReflectionTestUtils.setField(jwtTokenUtilService, "clusterApiSecret", "token");

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> jwtTokenUtilService.afterPropertiesSet());
    exception.isInstanceOf(Exception.class);
    exception.hasMessage("Invalid Base64 value configured. klaw.clusterapi.access.base64.secret");
  }
}
