package io.aiven.klaw.clusterapi;

import static io.aiven.klaw.clusterapi.models.enums.ClusterStatus.OFFLINE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = KafkaClusterApiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext
public class ClusterConnectivityIT {

  public static final String KWCLUSTERAPIUSER = "kwclusterapiuser";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterAccessSecret;

  @MockBean RestTemplate restTemplate;

  @Autowired private MockMvc mvc;

  @Test
  @Order(1)
  public void verifySchemaRegistryConnection() throws Exception {
    String url =
        "/topics/getStatus/localhost:8083/ "
            + KafkaSupportedProtocol.SSL
            + "/SRDEV1/schemaregistry";
    when(restTemplate.getForEntity(anyString(), any(), anyMap())).thenReturn(null);
    MockHttpServletResponse response =
        mvc.perform(
                MockMvcRequestBuilders.get(url)
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
    assertThat(clusterStatus).isEqualTo(OFFLINE);
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
