package io.aiven.klaw.clusterapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.connect.ConnectorState;
import io.aiven.klaw.clusterapi.models.connect.ConnectorsStatus;
import io.aiven.klaw.clusterapi.models.connect.Status;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.springtest.MockServerTest;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@MockServerTest
@Slf4j
public class ConnectControllerIT {
  public static final String KWCLUSTERAPIUSER = "kwclusterapiuser";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private MockServerClient mockServerClient;

  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterAccessSecret;

  private String CONNECT_REST_API;

  @Autowired private MockMvc mvc;

  @Autowired ObjectMapper objectMapper;
  private static UtilMethods utilMethods;

  static boolean bindPort = false;

  @BeforeEach
  public void setup() {
    int port = 56566;
    utilMethods = new UtilMethods();
    if (!bindPort) mockServerClient.bind(port);
    bindPort = true;
    CONNECT_REST_API = "127.0.0.1:" + port;
  }

  @Test
  @Order(1)
  public void getConnectors() throws Exception {
    Map<String, Map<String, Status>> responseMap = utilMethods.getConnectorsListMap();

    mockServerClient
        .when(request().withPath("/connectors").withQueryStringParameter("expand", "status"))
        .respond(
            response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                .withBody(objectMapper.writeValueAsString(responseMap)));

    String url = "/topics/getAllConnectors/" + CONNECT_REST_API + "/SSL/DEVCON1";
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
    ConnectorsStatus connectorsStatusResponse =
        OBJECT_MAPPER.readValue(response.getContentAsString(), ConnectorsStatus.class);
    assertThat(connectorsStatusResponse.getConnectorStateList().size()).isEqualTo(2);
    assertThat(connectorsStatusResponse.getConnectorStateList())
        .extracting(ConnectorState::getConnectorStatus)
        .contains("RUNNING")
        .contains("RUNNING");
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
