package io.aiven.klaw.clusterapi.services;

import static org.mockito.ArgumentMatchers.any;

import io.aiven.klaw.clusterapi.constants.TestConstants;
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
class ConsumerGroupServiceTest {

  @Mock private ClusterApiUtils clusterApiUtils;
  @Mock private KafkaSupportedProtocol protocol;
  @Mock private AdminClient adminClient;
  @Mock private DescribeTopicsResult describeTopicsResult;
  @Mock private TopicPartitionInfo topicPartitionInfo;
  @Mock private ListOffsetsResult listOffsetsEarliestResult;
  private ConsumerGroupService consumerGroupService;

  @BeforeEach
  void setUp() {
    consumerGroupService = new ConsumerGroupService(clusterApiUtils);
  }

  @Test
  void getConsumerGroupDetails() throws Exception {
    int offset = 1;
    List<TopicPartitionInfo> partitions = List.of(topicPartitionInfo);
    TopicDescription topicDescription = new TopicDescription("name", false, partitions);
    Map<String, KafkaFuture<TopicDescription>> nameTopicDescriptionFutures =
        Map.of(TestConstants.TOPIC_NAME, KafkaFuture.completedFuture(topicDescription));
    TopicPartition topicPartition =
        new TopicPartition(TestConstants.TOPIC_NAME, TestConstants.SINGLE_PARTITION);
    KafkaFuture<ListOffsetsResult.ListOffsetsResultInfo> listOffsetResultInfoFutures =
        KafkaFuture.completedFuture(
            new ListOffsetsResult.ListOffsetsResultInfo(offset, 1, Optional.of(1)));
    OffsetDetails offsetDetails = new OffsetDetails();
    offsetDetails.setTopicPartitionId(Long.toString(topicPartition.partition()));
    offsetDetails.setCurrentOffset(Long.toString(offset));
    offsetDetails.setEndOffset(Long.toString(offset));
    offsetDetails.setLag(Long.toString(0));

    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_NAME))
        .thenReturn(adminClient);
    Mockito.when(adminClient.describeTopics(Collections.singletonList(TestConstants.TOPIC_NAME)))
        .thenReturn(describeTopicsResult);
    Mockito.when(describeTopicsResult.values()).thenReturn(nameTopicDescriptionFutures);
    Mockito.when(topicPartitionInfo.partition()).thenReturn(TestConstants.SINGLE_PARTITION);
    Mockito.when(adminClient.listOffsets(any())).thenReturn(listOffsetsEarliestResult);
    Mockito.when(listOffsetsEarliestResult.partitionResult(any()))
        .thenReturn(listOffsetResultInfoFutures);

    List<OffsetDetails> actual =
        consumerGroupService.getConsumerGroupDetails(
            TestConstants.CONSUMER_GROUP_ID,
            TestConstants.TOPIC_NAME,
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_NAME);
    List<OffsetDetails> expected = List.of(offsetDetails);

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void getConsumerGroupDetailsFailure() throws Exception {
    Mockito.when(
            clusterApiUtils.getAdminClient(
                TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_NAME))
        .thenReturn(adminClient);

    List<OffsetDetails> actual =
        consumerGroupService.getConsumerGroupDetails(
            TestConstants.CONSUMER_GROUP_ID,
            TestConstants.TOPIC_NAME,
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_NAME);
    List<OffsetDetails> expected = Collections.emptyList();

    Assertions.assertThat(actual).isEqualTo(expected);
  }
}
