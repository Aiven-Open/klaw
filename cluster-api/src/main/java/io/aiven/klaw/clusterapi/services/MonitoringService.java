package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitoringService {

  private static final long timeOutSecsForAcls = 5;
  final ClusterApiUtils clusterApiUtils;

  public MonitoringService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public List<Map<String, String>> getConsumerGroupDetails(
      String consumerGroupId,
      String topicName,
      String environment,
      KafkaSupportedProtocol protocol,
      String clusterName)
      throws Exception {
    log.info(
        "getConsumerGroupDetails topicName {} consumerGroupId {} environment {} protocol {} clusterName {}",
        topicName,
        consumerGroupId,
        environment,
        protocol,
        clusterName);

    AdminClient adminClient = clusterApiUtils.getAdminClient(environment, protocol, clusterName);

    List<Map<String, String>> consumerGroupOffsetList = new ArrayList<>();
    Map<String, String> offsetDetails;
    try {
      DescribeTopicsResult describeTopicsResult =
          adminClient.describeTopics(Collections.singletonList(topicName));
      List<TopicPartitionInfo> topicPartitions =
          describeTopicsResult.values().get(topicName).get().partitions();

      TopicPartition topicPartition;
      Map<TopicPartition, OffsetSpec> topicPartitionOffsetSpecMap = new HashMap<>();

      for (TopicPartitionInfo topicPartitionInfo : topicPartitions) {
        topicPartition = new TopicPartition(topicName, topicPartitionInfo.partition());
        topicPartitionOffsetSpecMap.put(topicPartition, OffsetSpec.earliest());
      }
      ListOffsetsResult listOffsetsEarliestResult =
          adminClient.listOffsets(topicPartitionOffsetSpecMap);

      topicPartitionOffsetSpecMap = new HashMap<>();
      for (TopicPartitionInfo topicPartitionInfo : topicPartitions) {
        topicPartition = new TopicPartition(topicName, topicPartitionInfo.partition());
        topicPartitionOffsetSpecMap.put(topicPartition, OffsetSpec.latest());
      }

      ListOffsetsResult listOffsetsLatestResult =
          adminClient.listOffsets(topicPartitionOffsetSpecMap);

      for (TopicPartitionInfo topicPartitionInfo : topicPartitions) {
        topicPartition = new TopicPartition(topicName, topicPartitionInfo.partition());

        offsetDetails = new HashMap<>();
        long earliestOffset =
            listOffsetsEarliestResult.partitionResult(topicPartition).get().offset();
        long latestOffset = listOffsetsLatestResult.partitionResult(topicPartition).get().offset();
        long lag = latestOffset - earliestOffset;

        offsetDetails.put("topicPartitionId", Long.toString(topicPartition.partition()));
        offsetDetails.put("currentOffset", Long.toString(earliestOffset));
        offsetDetails.put("endOffset", Long.toString(latestOffset));
        offsetDetails.put("lag", Long.toString(lag));
        consumerGroupOffsetList.add(offsetDetails);
      }
      return consumerGroupOffsetList;
    } catch (Exception exception) {
      log.error(
          "Cannot retrieve consumer offset details topicName: {} groupid: {} Error: {}",
          topicName,
          consumerGroupId,
          exception);
      return consumerGroupOffsetList;
    }
  }
}
