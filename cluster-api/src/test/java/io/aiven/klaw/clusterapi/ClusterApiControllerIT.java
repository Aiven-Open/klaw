package io.aiven.klaw.clusterapi;

import static io.aiven.klaw.clusterapi.models.enums.ClusterStatus.ONLINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetResetType;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetsTiming;
import io.aiven.klaw.clusterapi.models.consumergroup.ResetConsumerGroupOffsetsRequest;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.RequestOperationType;
import io.aiven.klaw.clusterapi.services.SchemaService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@Slf4j
public class ClusterApiControllerIT {

  public static final String TOPIC_NAME = "testtopic";
  public static final String CONSUMER_GROUP = "testconsumergroup";
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
  ObjectMapper mapper = new ObjectMapper();

  @MockBean SchemaService schemaService;

  @Test
  @Order(1)
  public void getKafkaServerStatus() throws Exception {
    String url =
        "/topics/getStatus/" + bootStrapServers + "/PLAINTEXT/DEV1/kafka/kafkaFlavor/Apache Kafka";
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
    String url =
        "/topics/getStatus/" + bootStrapServersSsl + "/SSL/DEV2/kafka/kafkaFlavor/Apache Kafka";
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
    ClusterTopicRequest clusterTopicRequest = createTopicRequest(topicName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    String url = "/topics/createTopics";
    executeCreateTopicRequest(jsonReq, url);

    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Set<String> topicsSet = adminClient.listTopics().names().get();
            assertThat(topicsSet).contains(topicName);
          } catch (InterruptedException | ExecutionException e) {
            log.error("Error : ", e);
          }
        });
  }

  @Test
  @Order(4)
  public void updateTopics() throws Exception {
    String topicName = "testtopic";
    ClusterTopicRequest clusterTopicRequest = updateTopicRequest(topicName);

    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    String url = "/topics/updateTopics";
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
    Thread.sleep(300);

    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            ConfigResource configResource =
                new ConfigResource(ConfigResource.Type.TOPIC, clusterTopicRequest.getTopicName());
            Config topicConfig =
                adminClient
                    .describeConfigs(Collections.singleton(configResource))
                    .all()
                    .get(10, TimeUnit.SECONDS)
                    .get(configResource);
            assertThat(topicConfig.get("compression.type").value()).isEqualTo("snappy");
          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error : ", e);
          }
        });
  }

  @Test
  @Order(5)
  public void createTopicsExistingTopicSameConfigSuccess() throws Exception {
    String topicName = "testtopic";
    ClusterTopicRequest clusterTopicRequest = createTopicRequest(topicName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    String url = "/topics/createTopics";
    MockHttpServletResponse response = executeCreateTopicRequest(jsonReq, url);
    ApiResponse apiResponse =
        new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<>() {});
    assertThat(apiResponse.isSuccess()).isTrue();
  }

  @Test
  @Order(6)
  public void createTopicsExistingTopicDifferentConfigFailure() throws Exception {
    String topicName = "testtopic";
    ClusterTopicRequest clusterTopicRequest = createTopicRequest(topicName);
    clusterTopicRequest.setReplicationFactor((short) 4);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    String url = "/topics/createTopics";
    MockHttpServletResponse response = executeCreateTopicRequest(jsonReq, url);
    ApiResponse apiResponse =
        new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<>() {});
    assertThat(apiResponse.getMessage()).contains("TopicExistsException");
  }

  @Test
  @Order(7)
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
    executeCreateTopicRequest(jsonReq, url);

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
  @Order(8)
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
    executeCreateTopicRequest(jsonReq, url);

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
  @Order(9)
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
    executeCreateTopicRequest(jsonReq, url);

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
  @Order(10)
  public void createAclConsumerPrincipal() throws Exception {
    String principle = "CN=host,OU=dept";

    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .clusterName("DEV2")
            .topicName(TOPIC_NAME)
            .env(bootStrapServersSsl)
            .protocol(KafkaSupportedProtocol.SSL)
            .aclSsl(principle)
            .aclNativeType(AclsNativeType.NATIVE.name())
            .aclIpPrincipleType(AclIPPrincipleType.PRINCIPAL.name())
            .aclType(AclType.CONSUMER.value)
            .consumerGroup(CONSUMER_GROUP)
            .isPrefixAcl(false)
            .requestOperationType(RequestOperationType.CREATE)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterAclRequest);
    String url = "/topics/createAcls";
    executeCreateTopicRequest(jsonReq, url);

    ResourcePatternFilter resourceFilter =
        new ResourcePatternFilter(ResourceType.TOPIC, TOPIC_NAME, PatternType.LITERAL);
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
        new ResourcePatternFilter(ResourceType.GROUP, CONSUMER_GROUP, PatternType.LITERAL);
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

  @Test
  @Order(11)
  public void resetConsumerOffsetsToEarliest() throws Exception {
    produceAndConsumeRecords(true); // produce 10 records and consume all records

    String url = "/topics/consumerGroupOffsets/reset/" + bootStrapServersSsl + "/SSL/" + "DEV2";
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder()
            .offsetResetType(OffsetResetType.EARLIEST)
            .consumerGroup(CONSUMER_GROUP)
            .topicName(TOPIC_NAME)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(resetConsumerGroupOffsetsRequest);

    String response =
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
            .getResponse()
            .getContentAsString();
    ApiResponse apiResponse = new ObjectMapper().readValue(response, ApiResponse.class);

    Map<OffsetsTiming, Map<String, Long>> offsetsResetResponse = (Map) apiResponse.getData();
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(offsetsResetResponse).hasSize(2);
    String offsetsBefore =
        offsetsResetResponse
                .get(OffsetsTiming.BEFORE_OFFSET_RESET.getValue())
                .get(TOPIC_NAME + "-0")
            + "";
    String offsetsAfter =
        offsetsResetResponse.get(OffsetsTiming.AFTER_OFFSET_RESET.getValue()).get(TOPIC_NAME + "-0")
            + "";

    assertThat(offsetsBefore).isEqualTo("10");
    String expectedOffsetAfterReset = "0";
    assertThat(offsetsAfter).isEqualTo(expectedOffsetAfterReset);

    // verify consumer group offset position from admin client
    Thread.sleep(300);
    Map<String, Long> currentOffsetPositionsMap = new TreeMap<>();
    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap =
                adminClient
                    .listConsumerGroupOffsets(CONSUMER_GROUP)
                    .partitionsToOffsetAndMetadata()
                    .get();
            for (TopicPartition topicPartition : topicPartitionOffsetAndMetadataMap.keySet()) {
              currentOffsetPositionsMap.put(
                  topicPartition.toString(),
                  topicPartitionOffsetAndMetadataMap.get(topicPartition).offset());
            }
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        });
    assertThat(currentOffsetPositionsMap.get(TOPIC_NAME + "-0") + "")
        .isEqualTo(expectedOffsetAfterReset);
  }

  @Test
  @Order(12)
  public void resetConsumerOffsetsToLatest() throws Exception {
    produceAndConsumeRecords(true); // produce 10 more records

    String url = "/topics/consumerGroupOffsets/reset/" + bootStrapServersSsl + "/SSL/" + "DEV2";
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder()
            .offsetResetType(OffsetResetType.LATEST)
            .consumerGroup(CONSUMER_GROUP)
            .topicName(TOPIC_NAME)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(resetConsumerGroupOffsetsRequest);

    String response =
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
            .getResponse()
            .getContentAsString();
    ApiResponse apiResponse = new ObjectMapper().readValue(response, ApiResponse.class);

    Map<OffsetsTiming, Map<String, Long>> offsetsResetResponse = (Map) apiResponse.getData();
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(offsetsResetResponse).hasSize(2);
    String offsetsBefore =
        offsetsResetResponse
                .get(OffsetsTiming.BEFORE_OFFSET_RESET.getValue())
                .get(TOPIC_NAME + "-0")
            + "";
    String offsetsAfter =
        offsetsResetResponse.get(OffsetsTiming.AFTER_OFFSET_RESET.getValue()).get(TOPIC_NAME + "-0")
            + "";

    String expectedOffsetsBeforeAfter = "20";
    assertThat(offsetsBefore).isEqualTo(expectedOffsetsBeforeAfter);
    assertThat(offsetsAfter).isEqualTo(expectedOffsetsBeforeAfter);

    // verify consumer group offset position from admin client
    Thread.sleep(300);
    Map<String, Long> currentOffsetPositionsMap = new TreeMap<>();
    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap =
                adminClient
                    .listConsumerGroupOffsets(CONSUMER_GROUP)
                    .partitionsToOffsetAndMetadata()
                    .get();
            for (TopicPartition topicPartition : topicPartitionOffsetAndMetadataMap.keySet()) {
              currentOffsetPositionsMap.put(
                  topicPartition.toString(),
                  topicPartitionOffsetAndMetadataMap.get(topicPartition).offset());
            }
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        });
    assertThat(currentOffsetPositionsMap.get(TOPIC_NAME + "-0") + "")
        .isEqualTo(expectedOffsetsBeforeAfter);
  }

  @Test
  @Order(13)
  public void resetConsumerOffsetsToLatestDontConsumeRecs() throws Exception {
    produceAndConsumeRecords(false); // produce 10 more records

    String url = "/topics/consumerGroupOffsets/reset/" + bootStrapServersSsl + "/SSL/" + "DEV2";
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder()
            .offsetResetType(OffsetResetType.LATEST)
            .consumerGroup(CONSUMER_GROUP)
            .topicName(TOPIC_NAME)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(resetConsumerGroupOffsetsRequest);

    String response =
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
            .getResponse()
            .getContentAsString();
    ApiResponse apiResponse = new ObjectMapper().readValue(response, ApiResponse.class);

    Map<OffsetsTiming, Map<String, Long>> offsetsResetResponse = (Map) apiResponse.getData();
    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(offsetsResetResponse).hasSize(2);
    String offsetsBefore =
        offsetsResetResponse
                .get(OffsetsTiming.BEFORE_OFFSET_RESET.getValue())
                .get(TOPIC_NAME + "-0")
            + "";
    String offsetsAfter =
        offsetsResetResponse.get(OffsetsTiming.AFTER_OFFSET_RESET.getValue()).get(TOPIC_NAME + "-0")
            + "";

    String expectedOffsetsBeforeAfter = "20";
    assertThat(offsetsBefore).isEqualTo(expectedOffsetsBeforeAfter);
    assertThat(offsetsAfter).isEqualTo("30");

    // verify consumer group offset position from admin client
    Thread.sleep(300);
    Map<String, Long> currentOffsetPositionsMap = new TreeMap<>();
    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap =
                adminClient
                    .listConsumerGroupOffsets(CONSUMER_GROUP)
                    .partitionsToOffsetAndMetadata()
                    .get();
            for (TopicPartition topicPartition : topicPartitionOffsetAndMetadataMap.keySet()) {
              currentOffsetPositionsMap.put(
                  topicPartition.toString(),
                  topicPartitionOffsetAndMetadataMap.get(topicPartition).offset());
            }
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        });
    assertThat(currentOffsetPositionsMap.get(TOPIC_NAME + "-0") + "").isEqualTo("30");
  }

  @Test
  @Order(14)
  public void deleteTopics() throws Exception {
    // Create a topic
    String topicName = "testtopic-todelete";
    ClusterTopicRequest clusterTopicRequest = createTopicRequest(topicName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    String url = "/topics/createTopics";
    executeCreateTopicRequest(jsonReq, url);

    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Set<String> topicsSet = adminClient.listTopics().names().get();
            assertThat(topicsSet).contains(topicName);
          } catch (InterruptedException | ExecutionException e) {
            log.error("Error : ", e);
          }
        });

    // Delete the topic
    clusterTopicRequest = clusterTopicRequest.toBuilder().deleteAssociatedSchema(true).build();
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(clusterTopicRequest);
    url = "/topics/deleteTopics";
    when(schemaService.deleteSchema(any()))
        .thenReturn(
            ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build());
    MockHttpServletResponse response = executeCreateTopicRequest(jsonReq, url);

    embeddedKafkaBroker.doWithAdmin(
        adminClient -> {
          try {
            Set<String> topicsSet = adminClient.listTopics().names().get();
            assertThat(topicsSet).doesNotContain(topicName);
          } catch (InterruptedException | ExecutionException e) {
            log.error("Error : ", e);
          }
        });

    ApiResponse apiResponse = mapper.readValue(response.getContentAsString(), ApiResponse.class);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(15)
  public void resetOffsetsNonExistingTopic() throws Exception {
    String url = "/topics/consumerGroupOffsets/reset/" + bootStrapServersSsl + "/SSL/" + "DEV2";
    String nonExistingTopic = "topicdoesnotexist";
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder()
            .offsetResetType(OffsetResetType.LATEST)
            .consumerGroup(CONSUMER_GROUP)
            .topicName(nonExistingTopic)
            .build();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(resetConsumerGroupOffsetsRequest);

    Exception thrown =
        Assertions.assertThrows(
            Exception.class,
            () ->
                mvc.perform(
                        MockMvcRequestBuilders.post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonReq)
                            .header(
                                AUTHORIZATION,
                                BEARER_PREFIX
                                    + generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());
    assertThat(thrown.getMessage()).contains("Topic " + nonExistingTopic + " does not exist.");
  }

  private void produceAndConsumeRecords(boolean consumeRecs)
      throws ExecutionException, InterruptedException {
    Properties configProperties = new Properties();
    String stringSerializer = "org.apache.kafka.common.serialization.StringSerializer";
    String stringDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

    configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
    configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, stringSerializer);
    configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, stringSerializer);
    Producer<String, String> producer = new KafkaProducer<>(configProperties);
    ProducerRecord<String, String> rec1 =
        new ProducerRecord<>(
            TOPIC_NAME, null, System.currentTimeMillis(), "testkey", "A test message.");

    // produce 10 events;
    for (int i = 0; i < 10; i++) {
      producer.send(rec1).get();
    }

    if (consumeRecs) {
      Properties consumerConfigProperties = new Properties();
      consumerConfigProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
      consumerConfigProperties.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, stringDeserializer);
      consumerConfigProperties.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, stringDeserializer);
      consumerConfigProperties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
      consumerConfigProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "CLI_ID_131");
      consumerConfigProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

      KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerConfigProperties);
      kafkaConsumer.subscribe(Collections.singletonList(TOPIC_NAME));

      ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);

      for (ConsumerRecord<String, String> record : records) {
        // processed recs
        break;
      }
      kafkaConsumer.commitSync();
      kafkaConsumer.close();
    }
  }

  private MockHttpServletResponse executeCreateTopicRequest(String jsonReq, String url)
      throws Exception {
    return mvc.perform(
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
  }

  private static ClusterTopicRequest createTopicRequest(String topicName) {
    return ClusterTopicRequest.builder()
        .clusterName("DEV2")
        .topicName(topicName)
        .env(bootStrapServersSsl)
        .protocol(KafkaSupportedProtocol.SSL)
        .partitions(1)
        .replicationFactor(Short.parseShort("1"))
        .build();
  }

  private static ClusterTopicRequest updateTopicRequest(String topicName) {
    Map<String, String> advancedConfig = new HashMap<>();
    advancedConfig.put("compression.type", "snappy");
    return ClusterTopicRequest.builder()
        .clusterName("DEV2")
        .topicName(topicName)
        .env(bootStrapServersSsl)
        .protocol(KafkaSupportedProtocol.SSL)
        .partitions(1)
        .replicationFactor(Short.parseShort("1"))
        .advancedTopicConfiguration(advancedConfig)
        .build();
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
