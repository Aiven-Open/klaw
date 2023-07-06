package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

import io.aiven.klaw.clusterapi.constants.TestConstants;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.TopicConfig;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.TopicExistsException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApacheKafkaTopicServiceTest {

  @Mock private ClusterApiUtils clusterApiUtils;
  @Mock private SchemaService schemaService;

  private ApacheKafkaTopicService apacheKafkaTopicService;

  @Mock private KafkaSupportedProtocol protocol;
  @Mock private AdminClient adminClient;
  @Mock private ListTopicsResult listTopicsResult;
  @Mock private DescribeTopicsResult describeTopicsResult;
  @Mock private TopicDescription topicDescription;
  @Mock private TopicPartitionInfo topicPartitionInfo;
  @Mock private CreateTopicsResult createTopicsResult;
  @Mock private DeleteTopicsResult deleteTopicsResult;
  @Mock private KafkaFuture<Void> kafkaFuture;

  private static Stream<Exception> exceptionProviderForCreateTopic() {
    return Stream.of(
        new RuntimeException(),
        new NumberFormatException(),
        new KafkaException(),
        new InterruptedException(),
        new ExecutionException(new RuntimeException()));
  }

  private static Stream<Exception> exceptionProviderForDeleteTopic() {
    return Stream.of(
        new RuntimeException(""),
        new KafkaException(""),
        new InterruptedException(""),
        new ExecutionException(new RuntimeException("")));
  }

  @BeforeEach
  void setUp() {
    apacheKafkaTopicService = new ApacheKafkaTopicService(clusterApiUtils, schemaService);
  }

  @Test
  void loadTopicsClientNull() throws Exception {
    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(null);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(
            () ->
                apacheKafkaTopicService.loadTopics(
                    TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION));

    exception.isInstanceOf(Exception.class);
    exception.hasMessage("Cannot connect to cluster.");
  }

  @Test
  void loadTopics() throws Exception {
    Map<String, TopicDescription> topicDescriptionsPerAdminClient =
        new HashMap<>(
            Map.of(
                "_confluent", topicDescription,
                "__connect", topicDescription,
                "_schemas", topicDescription,
                "topic", topicDescription));

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.listTopics(any(ListTopicsOptions.class))).thenReturn(listTopicsResult);
    Mockito.when(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(Set.of("name")));
    Mockito.when(adminClient.describeTopics(any(Collection.class)))
        .thenReturn(describeTopicsResult);
    Mockito.when(describeTopicsResult.all())
        .thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
    Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));

    Set<TopicConfig> topicConfigs =
        apacheKafkaTopicService.loadTopics(
            TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION);

    Assertions.assertThat(topicConfigs.size()).isEqualTo(1);
  }

  @Test
  void createTopicClientNull() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(null);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> apacheKafkaTopicService.createTopic(clusterTopicRequest));

    exception.isInstanceOf(Exception.class);
    exception.hasMessage("Cannot connect to cluster.");
  }

  @Test
  void createTopic() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.MULTIPLE_PARTITIONS)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
    Mockito.when(createTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, KafkaFuture.completedFuture(null)));

    ApiResponse response = apacheKafkaTopicService.createTopic(clusterTopicRequest);

    Assertions.assertThat(response.isSuccess()).isTrue();
    Assertions.assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void createTopicTopicExistsException() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.MULTIPLE_PARTITIONS)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .build();
    Map<String, TopicDescription> topicDescriptionsPerAdminClient =
        new HashMap<>(Map.of(TestConstants.TOPIC_NAME, topicDescription));
    Exception expected = new ExecutionException(new TopicExistsException(TestConstants.TOPIC_NAME));

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
    Mockito.when(createTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, kafkaFuture));
    Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);
    Mockito.when(adminClient.describeTopics(any(Collection.class)))
        .thenReturn(describeTopicsResult);
    Mockito.when(describeTopicsResult.all())
        .thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
    Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));

    ApiResponse response = apacheKafkaTopicService.createTopic(clusterTopicRequest);

    Assertions.assertThat(response.isSuccess()).isFalse();
    Assertions.assertThat(response.getMessage()).isEqualTo(expected.getMessage());
  }

  @Test
  void createTopicTopicExistsWithSameConfig() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.SINGLE_PARTITION)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .build();
    Map<String, TopicDescription> topicDescriptionsPerAdminClient =
        new HashMap<>(Map.of(TestConstants.TOPIC_NAME, topicDescription));
    Exception expected = new ExecutionException(new TopicExistsException(TestConstants.TOPIC_NAME));

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
    Mockito.when(createTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, kafkaFuture));
    Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);
    Mockito.when(adminClient.describeTopics(any(Collection.class)))
        .thenReturn(describeTopicsResult);
    Mockito.when(describeTopicsResult.all())
        .thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
    Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));
    Mockito.when(topicPartitionInfo.replicas()).thenReturn(List.of(mock(Node.class)));

    ApiResponse response = apacheKafkaTopicService.createTopic(clusterTopicRequest);

    Assertions.assertThat(response.isSuccess()).isTrue();
    Assertions.assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @ParameterizedTest
  @MethodSource("exceptionProviderForCreateTopic")
  void createTopicException(Exception expected) throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.MULTIPLE_PARTITIONS)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
    Mockito.when(createTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, kafkaFuture));
    Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> apacheKafkaTopicService.createTopic(clusterTopicRequest));
    exception.isEqualTo(expected);
  }

  @Test
  void updateTopicClientNull() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(null);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> apacheKafkaTopicService.updateTopic(clusterTopicRequest));

    exception.isInstanceOf(Exception.class);
    exception.hasMessage("Cannot connect to cluster.");
  }

  @Test
  void updateTopic() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.MULTIPLE_PARTITIONS)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .build();
    Map<String, TopicDescription> topicDescriptionsPerAdminClient =
        new HashMap<>(Map.of(TestConstants.TOPIC_NAME, topicDescription));

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.describeTopics(any(Collection.class)))
        .thenReturn(describeTopicsResult);
    Mockito.when(describeTopicsResult.all())
        .thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
    Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));
    Mockito.when(adminClient.createPartitions(anyMap())).thenReturn(null);

    ApiResponse response = apacheKafkaTopicService.updateTopic(clusterTopicRequest);

    Assertions.assertThat(response.isSuccess()).isTrue();
    Assertions.assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void deleteTopicClientNull() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(null);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> apacheKafkaTopicService.deleteTopic(clusterTopicRequest));

    exception.isInstanceOf(Exception.class);
    exception.hasMessage("Cannot connect to cluster.");
  }

  @Test
  void deleteTopic() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.SINGLE_PARTITION)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .deleteAssociatedSchema(true)
            .build();
    ApiResponse schemaApiResponse =
        ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.deleteTopics(any(Collection.class))).thenReturn(deleteTopicsResult);
    Mockito.when(deleteTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, KafkaFuture.completedFuture(null)));
    Mockito.when(schemaService.deleteSchema(clusterTopicRequest)).thenReturn(schemaApiResponse);

    ApiResponse response = apacheKafkaTopicService.deleteTopic(clusterTopicRequest);

    Assertions.assertThat(response.isSuccess()).isTrue();
    Assertions.assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void deleteTopicUnknownTopicOrPartitionException() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.SINGLE_PARTITION)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .deleteAssociatedSchema(true)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.deleteTopics(any(Collection.class))).thenReturn(deleteTopicsResult);
    Mockito.when(deleteTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, kafkaFuture));
    Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS))
        .thenThrow(new InterruptedException("UnknownTopicOrPartition"));

    ApiResponse response = apacheKafkaTopicService.deleteTopic(clusterTopicRequest);

    Assertions.assertThat(response.isSuccess()).isTrue();
    Assertions.assertThat(response.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @ParameterizedTest
  @MethodSource("exceptionProviderForDeleteTopic")
  void deleteTopicException(Exception expected) throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env(TestConstants.ENVIRONMENT)
            .clusterName(TestConstants.CLUSTER_IDENTIFICATION)
            .protocol(protocol)
            .topicName(TestConstants.TOPIC_NAME)
            .partitions(TestConstants.SINGLE_PARTITION)
            .replicationFactor(TestConstants.REPLICATION_FACTOR)
            .advancedTopicConfiguration(TestConstants.ADVANCED_TOPIC_CONFIGURATION)
            .deleteAssociatedSchema(true)
            .build();

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(adminClient);
    Mockito.when(adminClient.deleteTopics(any(Collection.class))).thenReturn(deleteTopicsResult);
    Mockito.when(deleteTopicsResult.values())
        .thenReturn(Map.of(TestConstants.TOPIC_NAME, kafkaFuture));
    Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> apacheKafkaTopicService.deleteTopic(clusterTopicRequest));
    exception.isEqualTo(expected);
  }
}
