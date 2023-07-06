package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

import io.aiven.klaw.clusterapi.constants.TestConstants;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

// TODO From my humble point of view more work is require dto fix the test setup.
// Maybe it is also worth considering redesigning some classes involved to better adhere to shallow
// depth of test principle.
@ExtendWith(MockitoExtension.class)
public class UtilComponentsServiceTest {

  @Mock private ClusterApiUtils clusterApiUtils;

  @Mock private Environment env;

  @Mock private AdminClient adminClient;

  @Mock private SchemaService schemaService;

  @Mock private KafkaConnectService kafkaConnectService;

  @Mock private ConfluentCloudApiService confluentCloudApiService;
  private UtilComponentsService utilComponentsService;

  @BeforeEach
  public void setUp() {
    utilComponentsService =
        new UtilComponentsService(
            env, clusterApiUtils, schemaService, kafkaConnectService, confluentCloudApiService);
  }

  @Test
  public void getStatus_ClusterTypeKafkaAndKafkaFlavorConfluentCloud() throws Exception {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "kafka";
    String kafkaFlavor = "Confluent Cloud";

    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);

    assertThat(result).isSameAs(ClusterStatus.ONLINE);
  }

  @Test
  public void getStatus_ClusterTypeKafkaAndKafkaFlavorNotConfluentCloudAndClientNotNull()
      throws Exception {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "kafka";
    String kafkaFlavor = "Apache Kafka";

    Mockito.when(
            clusterApiUtils.getAdminClient(
                any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);
    assertThat(result).isSameAs(ClusterStatus.ONLINE);
  }

  @Test
  public void getStatus_ClusterTypeKafkaAndKafkaFlavorNotConfluentCloudAndClientNull()
      throws Exception {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "kafka";
    String kafkaFlavor = "Apache Kafka";

    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);

    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void getStatus_ClusterTypeKafkaAndExceptionThrown() throws Exception {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "kafka";
    String kafkaFlavor = "Apache Kafka";

    Mockito.when(
            clusterApiUtils.getAdminClient(
                any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenThrow(new Exception("error occured"));

    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);

    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void getStatus_ClusterTypeNull() {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "default";
    String kafkaFlavor = "Confluent Cloud";

    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);

    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void getStatus_ClusterTypeSchemaRegistry() {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "schemaregistry";
    String kafkaFlavor = "Apache Kafka";

    Mockito.when(
            schemaService.getSchemaRegistryStatus(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(ClusterStatus.ONLINE);

    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);

    assertThat(result).isSameAs(ClusterStatus.ONLINE);
  }

  @Test
  public void getStatus_ClusterTypeKafkaConnect() {
    KafkaSupportedProtocol protocol = KafkaSupportedProtocol.PLAINTEXT;
    String clusterType = "kafkaconnect";
    String kafkaFlavor = "Apache Kafka";

    Mockito.when(
            kafkaConnectService.getKafkaConnectStatus(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(ClusterStatus.ONLINE);

    ClusterStatus result =
        utilComponentsService.getStatus(
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_IDENTIFICATION,
            clusterType,
            kafkaFlavor);

    assertThat(result).isSameAs(ClusterStatus.ONLINE);
  }

  private Map<String, TopicDescription> getTopicDescs() {
    Node node = new Node(1, "localhost", 1);

    TopicPartitionInfo topicPartitionInfo =
        new TopicPartitionInfo(2, node, List.of(node), List.of(node));
    TopicDescription tDesc =
        new TopicDescription(
            "testtopic", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    Map<String, TopicDescription> mapResults = new HashMap<>();
    mapResults.put("testtopic1", tDesc);

    tDesc =
        new TopicDescription(
            "testtopic2", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    mapResults.put("testtopic2", tDesc);

    return mapResults;
  }
}
