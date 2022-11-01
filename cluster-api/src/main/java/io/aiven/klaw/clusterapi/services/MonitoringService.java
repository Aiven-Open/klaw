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

  final ClusterApiUtils clusterApiUtils;

  private static final long timeOutSecsForAcls = 5;

  public MonitoringService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public List<HashMap<String, String>> getConsumerGroupDetails(
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

    List<HashMap<String, String>> consumerGroupOffsetList = new ArrayList<>();
    HashMap<String, String> offsetDetails;
    try {
      DescribeTopicsResult describeTopicsResult =
          adminClient.describeTopics(Collections.singletonList(topicName));
      List<TopicPartitionInfo> topicPartitions =
          describeTopicsResult.values().get(topicName).get().partitions();

      TopicPartition topicPartition;
      Map<TopicPartition, OffsetSpec> topicPartitionOffsetSpecMap = new HashMap<>();
      topicPartitionOffsetSpecMap = new HashMap<>();

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

        offsetDetails.put("topicPartitionId", topicPartition.partition() + "");
        offsetDetails.put("currentOffset", earliestOffset + "");
        offsetDetails.put("endOffset", latestOffset + "");
        offsetDetails.put("lag", lag + "");
        consumerGroupOffsetList.add(offsetDetails);
      }
      return consumerGroupOffsetList;
    } catch (Exception exception) {
      log.error(
          "Cannot retrieve consumer offset details topicName: {} groupid: {} Error: {}",
          topicName,
          consumerGroupId,
          exception.toString());
      return consumerGroupOffsetList;
    }
  }
}
