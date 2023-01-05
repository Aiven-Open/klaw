package io.aiven.klaw.clusterapi;

import static io.aiven.klaw.clusterapi.models.enums.ClusterStatus.ONLINE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = KafkaClusterApiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext
public class AuthenticationIntegrationIT {

  public static final String KWCLUSTERAPIUSER = "kwclusterapiuser";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterAccessSecret;

  @Autowired private MockMvc mvc;

  @Test
  @Order(1)
  public void getApiStatus_Success() throws Exception {
    MockHttpServletResponse response =
        mvc.perform(
                MockMvcRequestBuilders.get("/topics/getApiStatus")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                        AUTHORIZATION,
                        BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

    ClusterStatus clusterStatus =
        OBJECT_MAPPER.readValue(response.getContentAsString(), ClusterStatus.class);
    assertThat(clusterStatus).isEqualTo(ONLINE);
  }

  @Test
  @Order(2)
  public void getApiStatus_InValidSubjectRequest() {
    RuntimeException thrown =
        Assertions.assertThrows(
            RuntimeException.class,
            () ->
                mvc.perform(
                        MockMvcRequestBuilders.get("/topics/getApiStatus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(
                                AUTHORIZATION,
                                BEARER_PREFIX
                                    + generateToken("notExistingUser", clusterAccessSecret, 3L))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andReturn()
                    .getResponse());
    assertThat(thrown.getMessage()).isEqualTo("User/subject not found !!");
  }

  @Test
  @Order(3)
  public void getApiStatus_InValidSecretRequest() throws Exception {
    String invalidSecret = "this is an invalid secret for the test";
    String base64EncodedSecret = Base64.encodeBase64String(invalidSecret.getBytes());
    mvc.perform(
            MockMvcRequestBuilders.get("/topics/getApiStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, base64EncodedSecret, 3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andReturn()
        .getResponse();
  }

  @Test
  @Order(4)
  public void getApiStatus_ExpiredToken() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.get("/topics/getApiStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, -3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andReturn()
        .getResponse();
  }

  private String generateToken(
      String clusterApiUser, String clusterAccessSecret, long expirationTime) {
    Key hmacKey =
        new SecretKeySpec(
            Base64.decodeBase64(clusterAccessSecret), SignatureAlgorithm.HS256.getJcaName());
    Instant now = Instant.now();

    return Jwts.builder()
        .claim("name", clusterApiUser)
        .setSubject(clusterApiUser)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(expirationTime, ChronoUnit.MINUTES)))
        .signWith(hmacKey)
        .compact();
  }
}
