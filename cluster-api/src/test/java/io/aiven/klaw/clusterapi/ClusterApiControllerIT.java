package io.aiven.klaw.clusterapi;

import static io.aiven.klaw.clusterapi.models.enums.ClusterStatus.ONLINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.RequestOperationType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
@EmbeddedKafka
public class ClusterApiControllerIT {

  public static final String KWCLUSTERAPIUSER = "kwclusterapiuser";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static EmbeddedKafkaBroker embeddedKafkaBroker;

  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterAccessSecret;

  private static final String bootStrapServers = "localhost:9092";
  private static final String bootStrapServersSsl = "localhost:9093";

  @Autowired private MockMvc mvc;

  @Test
  @Order(1)
  public void getKafkaServerStatus() throws Exception {
    String url = "/topics/getStatus/" + bootStrapServers + "/PLAINTEXT/DEV1/kafka";
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
    assertThat(clusterStatus).isEqualTo(ONLINE);
  }

  @Test
  @Order(2)
  public void getKafkaServerStatusSSL() throws Exception {
    String url = "/topics/getStatus/" + bootStrapServersSsl + "/SSL/DEV2/kafka";
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
    assertThat(clusterStatus).isEqualTo(ONLINE);
  }

  @Test
  @Order(3)
  public void createTopics() throws Exception {
    String topicName = "testtopic";
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .clusterName("DEV2")
            .topicName(topicName)
            .env(bootStrapServersSsl)
            .protocol(KafkaSupportedProtocol.SSL)
            .partitions(1)
            .replicationFactor(Short.parseShort("1"))
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    String url = "/topics/createTopics";
    mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReq)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Set<String> topicsSet = adminClient.listTopics().names().get();
            assertThat(topicsSet).contains(topicName);
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
        });
  }

  @Test
  @Order(4)
  public void createAclProducerIPAddress() throws Exception {
    String topicName = "testtopic";
    String ipHost = "11.12.13.14";
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .clusterName("DEV2")
            .topicName(topicName)
            .env(bootStrapServersSsl)
            .protocol(KafkaSupportedProtocol.SSL)
            .aclIp(ipHost)
            .aclNativeType(AclsNativeType.NATIVE.name())
            .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
            .aclType(AclType.PRODUCER.value)
            .isPrefixAcl(false)
            .requestOperationType(RequestOperationType.CREATE)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterAclRequest);
    String url = "/topics/createAcls";
    mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReq)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    ResourcePatternFilter resourceFilter =
        new ResourcePatternFilter(ResourceType.TOPIC, topicName, PatternType.LITERAL);
    final List<KafkaFuture<Collection<AclBinding>>> aclBindingFutureList = new ArrayList<>();

    Thread.sleep(300);
    embeddedKafkaBroker.doWithAdmin(
        adminClient ->
            aclBindingFutureList.add(
                adminClient
                    .describeAcls(
                        new AclBindingFilter(
                            resourceFilter,
                            new AccessControlEntryFilter(
                                "User:*", ipHost, AclOperation.WRITE, AclPermissionType.ALLOW)))
                    .values()));
    Collection<AclBinding> aclBindings = aclBindingFutureList.get(0).get();

    assertThat(aclBindings.size()).isEqualTo(1);
  }

  @Test
  @Order(5)
  public void createAclConsumerIPAddress() throws Exception {
    String topicName = "testtopic";
    String ipHost = "11.12.13.14";
    String consumerGroup = "testconsumergroup";
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .clusterName("DEV2")
            .topicName(topicName)
            .env(bootStrapServersSsl)
            .protocol(KafkaSupportedProtocol.SSL)
            .aclIp(ipHost)
            .aclNativeType(AclsNativeType.NATIVE.name())
            .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
            .aclType(AclType.CONSUMER.value)
            .consumerGroup(consumerGroup)
            .isPrefixAcl(false)
            .requestOperationType(RequestOperationType.CREATE)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterAclRequest);
    String url = "/topics/createAcls";
    mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReq)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    ResourcePatternFilter resourceFilter =
        new ResourcePatternFilter(ResourceType.TOPIC, topicName, PatternType.LITERAL);
    final List<KafkaFuture<Collection<AclBinding>>> aclBindingFutureList = new ArrayList<>();

    // verify topic read access
    Thread.sleep(300);
    embeddedKafkaBroker.doWithAdmin(
        adminClient ->
            aclBindingFutureList.add(
                adminClient
                    .describeAcls(
                        new AclBindingFilter(
                            resourceFilter,
                            new AccessControlEntryFilter(
                                "User:*", ipHost, AclOperation.READ, AclPermissionType.ALLOW)))
                    .values()));
    Collection<AclBinding> aclBindings = aclBindingFutureList.get(0).get();

    assertThat(aclBindings.size()).isEqualTo(1);

    // verify consumer group read access
    ResourcePatternFilter resourceFilter1 =
        new ResourcePatternFilter(ResourceType.GROUP, consumerGroup, PatternType.LITERAL);
    List<KafkaFuture<Collection<AclBinding>>> aclBindingFutureList1 = new ArrayList<>();

    Thread.sleep(300);
    embeddedKafkaBroker.doWithAdmin(
        adminClient ->
            aclBindingFutureList1.add(
                adminClient
                    .describeAcls(
                        new AclBindingFilter(
                            resourceFilter1,
                            new AccessControlEntryFilter(
                                "User:*", ipHost, AclOperation.READ, AclPermissionType.ALLOW)))
                    .values()));
    Collection<AclBinding> aclBindings1 = aclBindingFutureList.get(0).get();

    assertThat(aclBindings1.size()).isEqualTo(1);
  }

  @Test
  @Order(6)
  public void createAclProducerPrincipal() throws Exception {
    String topicName = "testtopic";
    String principle = "CN=host,OU=dept";
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .clusterName("DEV2")
            .topicName(topicName)
            .env(bootStrapServersSsl)
            .protocol(KafkaSupportedProtocol.SSL)
            .aclSsl(principle)
            .aclNativeType(AclsNativeType.NATIVE.name())
            .aclIpPrincipleType(AclIPPrincipleType.PRINCIPAL.name())
            .aclType(AclType.PRODUCER.value)
            .isPrefixAcl(false)
            .requestOperationType(RequestOperationType.CREATE)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterAclRequest);
    String url = "/topics/createAcls";
    mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReq)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    ResourcePatternFilter resourceFilter =
        new ResourcePatternFilter(ResourceType.TOPIC, topicName, PatternType.LITERAL);
    final List<KafkaFuture<Collection<AclBinding>>> aclBindingFutureList = new ArrayList<>();

    Thread.sleep(300);
    embeddedKafkaBroker.doWithAdmin(
        adminClient ->
            aclBindingFutureList.add(
                adminClient
                    .describeAcls(
                        new AclBindingFilter(
                            resourceFilter,
                            new AccessControlEntryFilter(
                                "User:" + principle,
                                "*",
                                AclOperation.WRITE,
                                AclPermissionType.ALLOW)))
                    .values()));
    Collection<AclBinding> aclBindings = aclBindingFutureList.get(0).get();

    assertThat(aclBindings.size()).isEqualTo(1);
  }

  @Test
  @Order(7)
  public void createAclConsumerPrincipal() throws Exception {
    String topicName = "testtopic";
    String principle = "CN=host,OU=dept";
    String consumerGroup = "testconsumergroup";
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .clusterName("DEV2")
            .topicName(topicName)
            .env(bootStrapServersSsl)
            .protocol(KafkaSupportedProtocol.SSL)
            .aclSsl(principle)
            .aclNativeType(AclsNativeType.NATIVE.name())
            .aclIpPrincipleType(AclIPPrincipleType.PRINCIPAL.name())
            .aclType(AclType.CONSUMER.value)
            .consumerGroup(consumerGroup)
            .isPrefixAcl(false)
            .requestOperationType(RequestOperationType.CREATE)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterAclRequest);
    String url = "/topics/createAcls";
    mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReq)
                .header(
                    AUTHORIZATION,
                    BEARER_PREFIX + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    ResourcePatternFilter resourceFilter =
        new ResourcePatternFilter(ResourceType.TOPIC, topicName, PatternType.LITERAL);
    final List<KafkaFuture<Collection<AclBinding>>> aclBindingFutureList = new ArrayList<>();

    // verify topic read access
    Thread.sleep(300);
    embeddedKafkaBroker.doWithAdmin(
        adminClient ->
            aclBindingFutureList.add(
                adminClient
                    .describeAcls(
                        new AclBindingFilter(
                            resourceFilter,
                            new AccessControlEntryFilter(
                                "User:" + principle,
                                "*",
                                AclOperation.READ,
                                AclPermissionType.ALLOW)))
                    .values()));
    Collection<AclBinding> aclBindings = aclBindingFutureList.get(0).get();

    assertThat(aclBindings.size()).isEqualTo(1);

    // verify consumer group read access
    ResourcePatternFilter resourceFilter1 =
        new ResourcePatternFilter(ResourceType.GROUP, consumerGroup, PatternType.LITERAL);
    List<KafkaFuture<Collection<AclBinding>>> aclBindingFutureList1 = new ArrayList<>();

    Thread.sleep(300);
    embeddedKafkaBroker.doWithAdmin(
        adminClient ->
            aclBindingFutureList1.add(
                adminClient
                    .describeAcls(
                        new AclBindingFilter(
                            resourceFilter1,
                            new AccessControlEntryFilter(
                                "User:" + principle,
                                "*",
                                AclOperation.READ,
                                AclPermissionType.ALLOW)))
                    .values()));
    Collection<AclBinding> aclBindings1 = aclBindingFutureList.get(0).get();

    assertThat(aclBindings1.size()).isEqualTo(1);
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

  private static Map<String, String> buildBrokerProperties() {
    Map<String, String> brokerProperties = new HashMap<>();

    brokerProperties.put("authorizer.class.name", "kafka.security.authorizer.AclAuthorizer");
    brokerProperties.put("allow.everyone.if.no.acl.found", "true");
    brokerProperties.put("super.users", "User:ANONYMOUS");
    brokerProperties.put(
        "ssl.truststore.location", "src/test/resources/selfsignedcerts/truststore.jks");
    brokerProperties.put("ssl.truststore.password", "klaw1234");
    brokerProperties.put(
        "ssl.keystore.location", "src/test/resources/selfsignedcerts/keystore.p12");
    brokerProperties.put("ssl.key.password", "klaw1234");
    brokerProperties.put("ssl.keystore.password", "klaw1234");
    brokerProperties.put("ssl.keystore.type", "pkcs12");
    brokerProperties.put(
        "listeners", "PLAINTEXT://" + bootStrapServers + ",SSL://" + bootStrapServersSsl);

    return brokerProperties;
  }

  @DynamicPropertySource
  static void registerKafkaProperties(DynamicPropertyRegistry registry) {
    embeddedKafkaBroker =
        new EmbeddedKafkaBroker(1, false, 1).brokerProperties(buildBrokerProperties());
    embeddedKafkaBroker.setAdminTimeout(100);
    embeddedKafkaBroker.afterPropertiesSet();
  }
}
