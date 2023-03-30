package io.aiven.klaw.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.Payload;
import io.jsonwebtoken.Jwts;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CustomJwtDecoderTest {

  CustomJwtDecoder customJwtDecoder;

  @BeforeEach
  public void setUp() throws Exception {
    customJwtDecoder = new CustomJwtDecoder();
  }

  @Test
  public void decode() {
    Jwt jwt = customJwtDecoder.decode(getSampleToken());
    assertThat(jwt.getClaims()).hasSize(6);
    assertThat(jwt.getClaims()).containsKeys("name", "email", "sub", "jti", "iat", "exp");
    assertThat(jwt.getClaims()).containsEntry("name", "Jane Doe");
  }

  @Test
  public void toJSONObject() {
    Payload payload = new Payload(getPayLoad());
    Map<String, Object> jsonMap = customJwtDecoder.toJSONObject(payload);
    assertThat(jsonMap.size()).isEqualTo(13); // ignores duplicates
  }

  private String getSampleToken() {
    return Jwts.builder()
        .claim("name", "Jane Doe")
        .claim("email", "jane@example.com")
        .setSubject("jane")
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(new Date())
        .setExpiration(Date.from(new Date().toInstant().plus(5, ChronoUnit.DAYS)))
        .compact();
  }

  // number of keys 14, with duplicate key (sub)
  private String getPayLoad() {
    return """
            {
              "exp": 1675788223,
              "iat": 1675787923,
              "auth_time": 1675787563,
              "jti": "c07d2db2f",
              "iss": "https://login.test.com/auth/realms/appid-123",
              "aud": "dbb-dev",
              "sub": "abcdefgh:test@test.com",
              "typ": "ID",
              "azp": "dbb-dev",
              "nonce": "fgdjkshfs",
              "session_state": "fds567534g78346",
              "acr": "0",
              "sub": "test@test.com",
              "groups": [
                "GRP-AuthUser",
                "GRP-admin"
              ]
            }""";
  }
}
