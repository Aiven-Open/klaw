package io.aiven.klaw.clusterapi.services;

import static org.mockito.ArgumentMatchers.any;

import io.aiven.klaw.clusterapi.models.OffsetDetails;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

  @Mock private ClusterApiUtils clusterApiUtils;
  @Mock private KafkaSupportedProtocol protocol;
  @Mock private AdminClient adminClient;
  @Mock private DescribeTopicsResult describeTopicsResult;
  @Mock private TopicPartitionInfo topicPartitionInfo;
  @Mock private ListOffsetsResult listOffsetsEarliestResult;
  private MonitoringService monitoringService;

  @BeforeEach
  void setUp() {
    monitoringService = new MonitoringService(clusterApiUtils);
  }

  @Test
  void getConsumerGroupDetails() throws Exception {
    String consumerGroupId = "consumerGroupId";
    String topicName = "topicName";
    String environment = "environment";
    String clusterName = "clusterName";
    int partition = 1;
    int offset = 1;
    List<TopicPartitionInfo> partitions = List.of(topicPartitionInfo);
    TopicDescription topicDescription = new TopicDescription("name", false, partitions);
    Map<String, KafkaFuture<TopicDescription>> nameTopicDescriptionFutures =
        Map.of("topicName", KafkaFuture.completedFuture(topicDescription));
    TopicPartition topicPartition = new TopicPartition(topicName, partition);
    KafkaFuture<ListOffsetsResult.ListOffsetsResultInfo> listOffsetResultInfoFutures =
        KafkaFuture.completedFuture(
            new ListOffsetsResult.ListOffsetsResultInfo(offset, 1, Optional.of(1)));
    OffsetDetails offsetDetails = new OffsetDetails();
    offsetDetails.setTopicPartitionId(Long.toString(topicPartition.partition()));
    offsetDetails.setCurrentOffset(Long.toString(offset));
    offsetDetails.setEndOffset(Long.toString(offset));
    offsetDetails.setLag(Long.toString(0));

    Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterName))
        .thenReturn(adminClient);
    Mockito.when(adminClient.describeTopics(Collections.singletonList(topicName)))
        .thenReturn(describeTopicsResult);
    Mockito.when(describeTopicsResult.values()).thenReturn(nameTopicDescriptionFutures);
    Mockito.when(topicPartitionInfo.partition()).thenReturn(partition);
    Mockito.when(adminClient.listOffsets(any())).thenReturn(listOffsetsEarliestResult);
    Mockito.when(listOffsetsEarliestResult.partitionResult(any()))
        .thenReturn(listOffsetResultInfoFutures);

    List<OffsetDetails> actual =
        monitoringService.getConsumerGroupDetails(
            consumerGroupId, topicName, environment, protocol, clusterName);
    List<OffsetDetails> expected = List.of(offsetDetails);

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void getConsumerGroupDetailsFailure() throws Exception {
    String consumerGroupId = "consumerGroupId";
    String topicName = "topicName";
    String environment = "environment";
    String clusterName = "clusterName";

    Mockito.when(clusterApiUtils.getAdminClient(environment, protocol, clusterName))
        .thenReturn(adminClient);

    List<OffsetDetails> actual =
        monitoringService.getConsumerGroupDetails(
            consumerGroupId, topicName, environment, protocol, clusterName);
    List<OffsetDetails> expected = Collections.emptyList();

    Assertions.assertThat(actual).isEqualTo(expected);
  }
}
