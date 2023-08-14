package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.constants.TestConstants;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetDetails;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetResetType;
import io.aiven.klaw.clusterapi.models.consumergroup.ResetConsumerGroupOffsetsRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

    when(clusterApiUtils.getAdminClient(
            TestConstants.ENVIRONMENT, protocol, TestConstants.CLUSTER_NAME))
        .thenReturn(adminClient);
    when(adminClient.describeTopics(Collections.singletonList(TestConstants.TOPIC_NAME)))
        .thenReturn(describeTopicsResult);
    when(describeTopicsResult.values()).thenReturn(nameTopicDescriptionFutures);
    when(topicPartitionInfo.partition()).thenReturn(TestConstants.SINGLE_PARTITION);
    when(adminClient.listOffsets(any())).thenReturn(listOffsetsEarliestResult);
    when(listOffsetsEarliestResult.partitionResult(any())).thenReturn(listOffsetResultInfoFutures);

    List<OffsetDetails> actual =
        consumerGroupService.getConsumerGroupDetails(
            TestConstants.CONSUMER_GROUP_ID,
            TestConstants.TOPIC_NAME,
            TestConstants.ENVIRONMENT,
            protocol,
            TestConstants.CLUSTER_NAME);
    List<OffsetDetails> expected = List.of(offsetDetails);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void getConsumerGroupDetailsFailure() throws Exception {
    when(clusterApiUtils.getAdminClient(
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

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void resetConsumerOffsetsToDateTimeFailure() {
    ResetConsumerGroupOffsetsRequest resetConsumerGroupOffsetsRequest =
        ResetConsumerGroupOffsetsRequest.builder()
            .offsetResetType(OffsetResetType.TO_DATE_TIME)
            .consumerGroup("CONSUMER_GROUP")
            .topicName("testtopic")
            .build();
    Exception thrown =
        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () ->
                consumerGroupService.resetConsumerGroupOffsets(
                    "dev", KafkaSupportedProtocol.SSL, "CLID2", resetConsumerGroupOffsetsRequest));
    assertThat(thrown.getMessage())
        .contains("Timestamp must be provided for reset type TO_DATE_TIME");
  }
}
