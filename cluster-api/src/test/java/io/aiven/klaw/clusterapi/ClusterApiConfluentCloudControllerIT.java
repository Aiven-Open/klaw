package io.aiven.klaw.clusterapi;

import static io.aiven.klaw.clusterapi.models.enums.ClusterStatus.ONLINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListAclsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListTopicsResponse;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
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
public class ClusterApiConfluentCloudControllerIT {

  public static final String KAFKA_V_3_CLUSTERS_CLUSTER_ID_TOPICS =
      "/kafka/v3/clusters/cluster_id/topics";
  public static final String KAFKA_V_3_CLUSTERS_CLUSTER_ID_ACLS =
      "/kafka/v3/clusters/cluster_id/acls";
  public static final String KWCLUSTERAPIUSER = "kwclusterapiuser";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private MockServerClient mockServerClient;

  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterAccessSecret;

  private String CONFLUENT_CLOUD_REST_API;

  @Autowired private MockMvc mvc;

  @Autowired ObjectMapper objectMapper;
  private static UtilMethods utilMethods;

  static boolean bindPort = false;

  @BeforeEach
  public void setup() {
    int port = 56565;
    utilMethods = new UtilMethods();
    if (!bindPort) mockServerClient.bind(port);
    bindPort = true;
    CONFLUENT_CLOUD_REST_API = "127.0.0.1:" + port;
  }

  @Test
  @Order(1)
  public void getKafkaServerStatus() throws Exception {
    ListTopicsResponse listTopicsResponse = utilMethods.getConfluentCloudListTopicsResponse();
    MockHttpServletResponse response;
    mockServerClient
        .when(request().withPath(KAFKA_V_3_CLUSTERS_CLUSTER_ID_TOPICS))
        .respond(
            response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                .withBody(objectMapper.writeValueAsString(listTopicsResponse)));

    String url =
        "/topics/getStatus/"
            + CONFLUENT_CLOUD_REST_API
            + "/SSL/DEV2/kafka/kafkaFlavor/Confluent Cloud";
    response =
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
    assertThat(clusterStatus).isEqualTo(ONLINE);
  }

  @Test
  @Order(2)
  public void getTopics() throws Exception {
    ListTopicsResponse listTopicsResponse = utilMethods.getConfluentCloudListTopicsResponse();
    MockHttpServletResponse response;
    mockServerClient
        .when(request().withPath(KAFKA_V_3_CLUSTERS_CLUSTER_ID_TOPICS))
        .respond(
            response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                .withBody(objectMapper.writeValueAsString(listTopicsResponse)));

    String url =
        "/topics/getTopics/"
            + CONFLUENT_CLOUD_REST_API
            + "/SSL/DEV2/topicsNativeType/CONFLUENT_CLOUD";
    response =
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

    Set<Map<String, String>> listTopicsSet =
        OBJECT_MAPPER.readValue(response.getContentAsString(), new TypeReference<>() {});
    assertThat(listTopicsSet).hasSize(2); // two topics
    assertThat(listTopicsSet.stream().toList().get(0))
        .hasSize(3); // topicName, partitions, replication factor
    assertThat(listTopicsSet.stream().toList().get(0))
        .containsKeys("topicName", "partitions", "replicationFactor");
  }

  @Test
  @Order(3)
  public void getAcls() throws Exception {
    ListAclsResponse listAclsResponse = utilMethods.getConfluentCloudListAclsResponse();
    MockHttpServletResponse response;
    mockServerClient
        .when(request().withPath(KAFKA_V_3_CLUSTERS_CLUSTER_ID_ACLS))
        .respond(
            response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                .withBody(objectMapper.writeValueAsString(listAclsResponse)));

    String url =
        "/topics/getAcls/" + CONFLUENT_CLOUD_REST_API + "/CONFLUENT_CLOUD/SSL/DEV2/null/null";
    response =
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

    Set<Map<String, String>> aclsSet =
        OBJECT_MAPPER.readValue(response.getContentAsString(), new TypeReference<>() {});
    assertThat(aclsSet).hasSize(2); // two acls
    assertThat(aclsSet.stream().toList().get(0))
        .hasSize(6); // operation, resourceType, resourceName, principle, host, permissionType
    assertThat(aclsSet.stream().toList().get(0))
        .containsKeys(
            "operation", "resourceType", "resourceName", "principle", "host", "permissionType");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("resourceName", "testtopic");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("permissionType", "ALLOW");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("resourceType", "TOPIC");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("host", "12.12.43.123");
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
