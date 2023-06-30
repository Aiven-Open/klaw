package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.TopicConfig;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

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

    @BeforeEach
    void setUp() {
        apacheKafkaTopicService = new ApacheKafkaTopicService(clusterApiUtils, schemaService);
    }

    @Test
    void loadTopicsClientNull() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(null);

        AbstractThrowableAssert<?, ? extends Throwable> exception =
                assertThatThrownBy(() -> apacheKafkaTopicService.loadTopics(environment, protocol, clusterIdentification));

        exception.isInstanceOf(Exception.class);
        exception.hasMessage("Cannot connect to cluster.");
    }

    @Test
    void loadTopics() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        Map<String, TopicDescription> topicDescriptionsPerAdminClient = new HashMap<>(Map.of(
                "_confluent", topicDescription,
                "__connect", topicDescription,
                "_schemas", topicDescription,
                "topic", topicDescription));

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.listTopics(any(ListTopicsOptions.class))).thenReturn(listTopicsResult);
        Mockito.when(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(Set.of("name")));
        Mockito.when(adminClient.describeTopics(any(Collection.class))).thenReturn(describeTopicsResult);
        Mockito.when(describeTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
        Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));

        Set<TopicConfig> topicConfigs = apacheKafkaTopicService.loadTopics(environment, protocol, clusterIdentification);

        Assertions.assertThat(topicConfigs.size()).isEqualTo(1);
    }

    @Test
    void createTopicClientNull() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .build();

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(null);

        AbstractThrowableAssert<?, ? extends Throwable> exception =
                assertThatThrownBy(() -> apacheKafkaTopicService.createTopic(clusterTopicRequest));

        exception.isInstanceOf(Exception.class);
        exception.hasMessage("Cannot connect to cluster.");
    }

    @Test
    void createTopic() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 5;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .build();

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
        Mockito.when(createTopicsResult.values()).thenReturn(Map.of(topicName, KafkaFuture.completedFuture(null)));

        ApiResponse response = apacheKafkaTopicService.createTopic(clusterTopicRequest);

        Assertions.assertThat(response.isSuccess()).isTrue();
        Assertions.assertThat(response.getMessage()).isEqualTo("success");
    }

    @Test
    void createTopicTopicExistsException() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 5;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .build();
        Map<String, TopicDescription> topicDescriptionsPerAdminClient = new HashMap<>(Map.of(topicName, topicDescription));
        Exception expected = new ExecutionException(new TopicExistsException(topicName));
        KafkaFuture<Void> kafkaFuture = mock(KafkaFuture.class);

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
        Mockito.when(createTopicsResult.values()).thenReturn(Map.of(topicName, kafkaFuture));
        Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);
        Mockito.when(adminClient.describeTopics(any(Collection.class))).thenReturn(describeTopicsResult);
        Mockito.when(describeTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
        Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));

        ApiResponse response = apacheKafkaTopicService.createTopic(clusterTopicRequest);

        Assertions.assertThat(response.isSuccess()).isFalse();
        Assertions.assertThat(response.getMessage()).isEqualTo(expected.getMessage());
    }

    @Test
    void createTopicTopicExistsWithSameConfig() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 1;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .build();
        Map<String, TopicDescription> topicDescriptionsPerAdminClient = new HashMap<>(Map.of(topicName, topicDescription));
        Exception expected = new ExecutionException(new TopicExistsException(topicName));
        KafkaFuture<Void> kafkaFuture = mock(KafkaFuture.class);

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
        Mockito.when(createTopicsResult.values()).thenReturn(Map.of(topicName, kafkaFuture));
        Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);
        Mockito.when(adminClient.describeTopics(any(Collection.class))).thenReturn(describeTopicsResult);
        Mockito.when(describeTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
        Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));
        Mockito.when(topicPartitionInfo.replicas()).thenReturn(List.of(mock(Node.class)));

        ApiResponse response = apacheKafkaTopicService.createTopic(clusterTopicRequest);

        Assertions.assertThat(response.isSuccess()).isTrue();
        Assertions.assertThat(response.getMessage()).isEqualTo("success");
    }

    @ParameterizedTest
    @MethodSource("exceptionProviderForCreateTopic")
    void createTopicException(Exception expected) throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 5;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .build();
        KafkaFuture<Void> kafkaFuture = mock(KafkaFuture.class);

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
        Mockito.when(createTopicsResult.values()).thenReturn(Map.of(topicName, kafkaFuture));
        Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);

        AbstractThrowableAssert<?, ? extends Throwable> exception =
                assertThatThrownBy(() -> apacheKafkaTopicService.createTopic(clusterTopicRequest));
        exception.isEqualTo(expected);
    }

    private static Stream<Exception> exceptionProviderForCreateTopic() {
        return Stream.of(new RuntimeException(), new NumberFormatException(),
                new KafkaException(), new InterruptedException(),
                new ExecutionException(new RuntimeException()));
    }

    @Test
    void updateTopicClientNull() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .build();

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(null);

        AbstractThrowableAssert<?, ? extends Throwable> exception =
                assertThatThrownBy(() -> apacheKafkaTopicService.updateTopic(clusterTopicRequest));

        exception.isInstanceOf(Exception.class);
        exception.hasMessage("Cannot connect to cluster.");
    }

    @Test
    void updateTopic() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 5;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .build();
        Map<String, TopicDescription> topicDescriptionsPerAdminClient = new HashMap<>(Map.of(topicName, topicDescription));

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.describeTopics(any(Collection.class))).thenReturn(describeTopicsResult);
        Mockito.when(describeTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(topicDescriptionsPerAdminClient));
        Mockito.when(topicDescription.partitions()).thenReturn(List.of(topicPartitionInfo));
        Mockito.when(adminClient.createPartitions(anyMap())).thenReturn(null);

        ApiResponse response = apacheKafkaTopicService.updateTopic(clusterTopicRequest);

        Assertions.assertThat(response.isSuccess()).isTrue();
        Assertions.assertThat(response.getMessage()).isEqualTo("success");
    }

    @Test
    void deleteTopicClientNull() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .build();

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(null);

        AbstractThrowableAssert<?, ? extends Throwable> exception =
                assertThatThrownBy(() -> apacheKafkaTopicService.deleteTopic(clusterTopicRequest));

        exception.isInstanceOf(Exception.class);
        exception.hasMessage("Cannot connect to cluster.");
    }

    @Test
    void deleteTopic() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 1;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .deleteAssociatedSchema(true)
                .build();
        ApiResponse schemaApiResponse = ApiResponse.builder().message("success").build();

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.deleteTopics(any(Collection.class))).thenReturn(deleteTopicsResult);
        Mockito.when(deleteTopicsResult.values()).thenReturn(Map.of(topicName, KafkaFuture.completedFuture(null)));
        Mockito.when(schemaService.deleteSchema(clusterTopicRequest)).thenReturn(schemaApiResponse);

        ApiResponse response = apacheKafkaTopicService.deleteTopic(clusterTopicRequest);

        Assertions.assertThat(response.isSuccess()).isTrue();
        Assertions.assertThat(response.getMessage()).isEqualTo("success");
    }

    @Test
    void deleteTopicUnknownTopicOrPartitionException() throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 1;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .deleteAssociatedSchema(true)
                .build();
        ApiResponse schemaApiResponse = ApiResponse.builder().message("success").build();
        KafkaFuture<Void> kafkaFuture = mock(KafkaFuture.class);

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.deleteTopics(any(Collection.class))).thenReturn(deleteTopicsResult);
        Mockito.when(deleteTopicsResult.values()).thenReturn(Map.of(topicName, kafkaFuture));
        Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(new InterruptedException("UnknownTopicOrPartition"));

        ApiResponse response = apacheKafkaTopicService.deleteTopic(clusterTopicRequest);

        Assertions.assertThat(response.isSuccess()).isTrue();
        Assertions.assertThat(response.getMessage()).isEqualTo("success");
    }

    @ParameterizedTest
    @MethodSource("exceptionProviderForDeleteTopic")
    void deleteTopicException(Exception expected) throws Exception {
        String environment = "ENVIRONMENT";
        String clusterIdentification = "CLUSTER_IDENTIFICATION";
        String topicName = "TOPIC_NAME";
        int partitions = 1;
        short replicationFactor = 1;
        Map<String, String> advancedTopicConfiguration = Map.of("topic", "config");
        ClusterTopicRequest clusterTopicRequest = ClusterTopicRequest.builder()
                .env(environment)
                .clusterName(clusterIdentification)
                .protocol(protocol)
                .topicName(topicName)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .advancedTopicConfiguration(advancedTopicConfiguration)
                .deleteAssociatedSchema(true)
                .build();
        ApiResponse schemaApiResponse = ApiResponse.builder().message("success").build();
        KafkaFuture<Void> kafkaFuture = mock(KafkaFuture.class);

        Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification)).thenReturn(adminClient);
        Mockito.when(adminClient.deleteTopics(any(Collection.class))).thenReturn(deleteTopicsResult);
        Mockito.when(deleteTopicsResult.values()).thenReturn(Map.of(topicName, kafkaFuture));
        Mockito.when(kafkaFuture.get(5, TimeUnit.SECONDS)).thenThrow(expected);

        AbstractThrowableAssert<?, ? extends Throwable> exception =
                assertThatThrownBy(() -> apacheKafkaTopicService.deleteTopic(clusterTopicRequest));
        exception.isEqualTo(expected);
    }

    private static Stream<Exception> exceptionProviderForDeleteTopic() {
        return Stream.of(new RuntimeException(""), new KafkaException(""),
                new InterruptedException(""),
                new ExecutionException(new RuntimeException("")));
    }
}