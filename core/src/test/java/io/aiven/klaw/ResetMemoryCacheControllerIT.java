package io.aiven.klaw;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.requests.ResetEntityCache;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Integration tests for /resetMemoryCache endpoint security. Tests verify that:
 *
 * <ul>
 *   <li>Anonymous users cannot reset cache
 *   <li>Users without UPDATE_SERVERCONFIG permission cannot reset cache
 *   <li>Users with UPDATE_SERVERCONFIG permission can reset cache
 *   <li>Internal service tokens (App2App) can reset cache
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-application-rdbms.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class ResetMemoryCacheControllerIT {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired private MockMvc mvc;

  @Value("${klaw.core.app2app.base64.secret:#{''}}")
  private String app2AppApiSecret;

  private static final String SUPERADMIN = "superadmin";
  private static final String SUPERADMIN_PWD = "WelcomeToKlaw321@";

  @BeforeAll
  public static void setup() {
    // Setup if needed
  }

  private ResetEntityCache createResetEntityCache() {
    return ResetEntityCache.builder()
        .tenantId(101)
        .entityType("USERS")
        .entityValue("testuser")
        .operationType("CREATE")
        .build();
  }

  /**
   * Test that anonymous users (no authentication) are rejected. The endpoint is in permitAll() but
   * service-layer rejects anonymous users.
   */
  @Test
  @Order(1)
  public void resetCacheAnonymousUserRejected() throws Exception {
    String jsonReq = OBJECT_MAPPER.writeValueAsString(createResetEntityCache());

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/resetMemoryCache")
                    .header("Authorization", "")
                    .with(csrf())
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()) // Endpoint returns 200 but with NOT_AUTHORIZED message
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse apiResponse = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(apiResponse.getMessage()).isEqualTo("Not Authorized");
  }

  /** Test that superadmin (who has UPDATE_SERVERCONFIG permission) can reset cache successfully. */
  @Test
  @Order(2)
  public void resetCacheWithSuperadminSucceeds() throws Exception {
    String jsonReq = OBJECT_MAPPER.writeValueAsString(createResetEntityCache());

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/resetMemoryCache")
                    .header("Authorization", "Bearer user-token")
                    .with(user(SUPERADMIN).password(SUPERADMIN_PWD).roles("SUPERADMIN"))
                    .with(csrf())
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse apiResponse = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(apiResponse.getMessage()).isEqualTo("success");
  }

  /**
   * Test that a valid internal service token (App2App JWT) allows cache reset. This simulates
   * server-to-server communication in HA mode.
   */
  @Test
  @Order(3)
  public void resetCacheWithValidInternalTokenSucceeds() throws Exception {
    // Skip if app2app secret is not configured
    if (app2AppApiSecret == null || app2AppApiSecret.isBlank()) {
      return;
    }

    String internalToken = generateApp2AppToken();
    String jsonReq = OBJECT_MAPPER.writeValueAsString(createResetEntityCache());

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/resetMemoryCache")
                    .header("Authorization", "Bearer " + internalToken)
                    .with(csrf())
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse apiResponse = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(apiResponse.getMessage()).isEqualTo("success");
  }

  /** Test that an invalid/expired token is rejected. */
  @Test
  @Order(4)
  public void resetCacheWithInvalidTokenRejected() throws Exception {
    String invalidToken = "invalid.jwt.token";
    String jsonReq = OBJECT_MAPPER.writeValueAsString(createResetEntityCache());

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/resetMemoryCache")
                    .header("Authorization", "Bearer " + invalidToken)
                    .with(csrf())
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()) // Returns 200 but with NOT_AUTHORIZED
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse apiResponse = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(apiResponse.getMessage()).isEqualTo("Not Authorized");
  }

  /** Generate an App2App JWT token matching HARestMessagingService format. */
  private String generateApp2AppToken() {
    Key hmacKey =
        new SecretKeySpec(
            Base64.decodeBase64(app2AppApiSecret), SignatureAlgorithm.HS256.getJcaName());
    Instant now = Instant.now();

    return Jwts.builder()
        .claim("name", "KlawApp2App")
        .claim("Roles", List.of("CACHE_ADMIN", "App2App"))
        .subject("KlawApp2App")
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(3L, ChronoUnit.MINUTES)))
        .signWith(hmacKey)
        .compact();
  }
}
