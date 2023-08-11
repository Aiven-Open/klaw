package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetDetails;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetResetType;
import io.aiven.klaw.clusterapi.models.consumergroup.OffsetsTiming;
import io.aiven.klaw.clusterapi.models.consumergroup.ResetConsumerGroupOffsetsRequest;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConsumerGroupOffsetsOptions;
import org.apache.kafka.clients.admin.AlterConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerGroupService {
  private final ClusterApiUtils clusterApiUtils;

  public ConsumerGroupService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public ApiResponse resetConsumerGroupOffsets(
      String environment,
      KafkaSupportedProtocol protocol,
      String clusterIdentification,
      ResetConsumerGroupOffsetsRequest consumerGroupOffsetsRequest)
      throws Exception {
    log.info(
        "Reset consumer group offsets env {} request {}", environment, consumerGroupOffsetsRequest);

    if (OffsetResetType.TO_DATE_TIME.equals(consumerGroupOffsetsRequest.getOffsetResetType())
        && null == consumerGroupOffsetsRequest.getConsumerGroupResetTimestampMilliSecs()) {
      throw new Exception("Timestamp must be provided for reset type TO_DATE_TIME");
    }
    AdminClient adminClient =
        clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification);

    TopicDescription topicDescription =
        describeTopic(adminClient, consumerGroupOffsetsRequest.getTopicName());
    if (null == topicDescription) {
      throw new Exception(
          "Topic " + consumerGroupOffsetsRequest.getTopicName() + " does not exist.");
    }

    OffsetSpec offsetSpec =
        switch (consumerGroupOffsetsRequest.getOffsetResetType()) {
          case EARLIEST -> OffsetSpec.earliest();
          case LATEST -> OffsetSpec.latest();
          case TO_DATE_TIME -> OffsetSpec.forTimestamp(
              consumerGroupOffsetsRequest.getConsumerGroupResetTimestampMilliSecs());
        };
    Map<OffsetsTiming, Map<String, Long>> offsetPositionsBeforeAndAfter = new HashMap<>();

    extractOffsetsBeforeAndAfter(
        consumerGroupOffsetsRequest,
        adminClient,
        topicDescription,
        offsetSpec,
        offsetPositionsBeforeAndAfter);

    return ApiResponse.builder().success(true).data(offsetPositionsBeforeAndAfter).build();
  }

  private void extractOffsetsBeforeAndAfter(
      ResetConsumerGroupOffsetsRequest consumerGroupOffsetsRequest,
      AdminClient adminClient,
      TopicDescription topicDescription,
      OffsetSpec offsetSpec,
      Map<OffsetsTiming, Map<String, Long>> offsetPositionsBeforeAndAfter)
      throws Exception {
    // Get offsets before consumer group offset update
    Map<String, Long> currentOffsets =
        getCurrentOffsetsPositions(consumerGroupOffsetsRequest.getConsumerGroup(), adminClient);
    if (!currentOffsets.isEmpty()) {
      offsetPositionsBeforeAndAfter.put(OffsetsTiming.BEFORE_OFFSET_RESET, currentOffsets);
    } else {
      return;
    }

    // reset offsets
    try {
      AlterConsumerGroupOffsetsOptions alterConsumerGroupOffsetsOptions =
          new AlterConsumerGroupOffsetsOptions();
      alterConsumerGroupOffsetsOptions.timeoutMs(2500);
      AlterConsumerGroupOffsetsResult futureResult =
          adminClient.alterConsumerGroupOffsets(
              consumerGroupOffsetsRequest.getConsumerGroup(),
              getTopicPartitionOffsetsAndMetadataMap(
                  consumerGroupOffsetsRequest.getTopicName(),
                  offsetSpec,
                  topicDescription,
                  adminClient),
              alterConsumerGroupOffsetsOptions);
      futureResult.all().get();
    } catch (Exception e) {
      throw new Exception("Unable to reset consumer group offsets.", e);
    }

    // Get offsets after consumer group offset update
    offsetPositionsBeforeAndAfter.put(
        OffsetsTiming.AFTER_OFFSET_RESET,
        getCurrentOffsetsPositions(consumerGroupOffsetsRequest.getConsumerGroup(), adminClient));
  }

  public Map<TopicPartition, OffsetAndMetadata> getTopicPartitionOffsetsAndMetadataMap(
      String topicName,
      OffsetSpec offsetSpec,
      TopicDescription topicDescription,
      AdminClient adminClient)
      throws ExecutionException, InterruptedException {
    Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = new HashMap<>();
    Map<TopicPartition, OffsetSpec> toOffsetSpec = new HashMap<>();

    List<TopicPartitionInfo> topicPartitionInfos = topicDescription.partitions();
    TopicPartition topicPartition;

    long partitionOffset;
    for (TopicPartitionInfo topicPartitionInfo : topicPartitionInfos) {
      topicPartition = new TopicPartition(topicName, topicPartitionInfo.partition());
      toOffsetSpec.put(topicPartition, offsetSpec);
      partitionOffset =
          adminClient.listOffsets(toOffsetSpec).partitionResult(topicPartition).get().offset();
      offsetAndMetadataMap.put(topicPartition, new OffsetAndMetadata(partitionOffset));
    }

    return offsetAndMetadataMap;
  }

  public Map<String, Long> getCurrentOffsetsPositions(String consumerGroup, AdminClient adminClient)
      throws Exception {
    Map<String, Long> currentOffsetPositionsMap = new TreeMap<>();
    KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> offsetsBeforeResetFuture =
        adminClient.listConsumerGroupOffsets(consumerGroup).partitionsToOffsetAndMetadata();
    try {
      Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap =
          offsetsBeforeResetFuture.get();
      for (TopicPartition topicPartition : topicPartitionOffsetAndMetadataMap.keySet()) {
        currentOffsetPositionsMap.put(
            topicPartition.toString(),
            topicPartitionOffsetAndMetadataMap.get(topicPartition).offset());
      }
      return currentOffsetPositionsMap;
    } catch (InterruptedException | ExecutionException e) {
      // ignore error as there may not be any events or couldn't retrieve events
      return new HashMap<>();
    }
  }

  public TopicDescription describeTopic(AdminClient adminClient, String topicName)
      throws Exception {
    try {
      DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topicName));
      if (result.values().containsKey(topicName)) {
        return result.values().get(topicName).get();
      } else {
        return null;
      }
    } catch (Exception ex) {
      if (ex.getLocalizedMessage().contains("UnknownTopicOrPartitionException")) {
        log.info("Topic {} does not exist on the cluster.", topicName);
        return null;
      } else {
        String errorMessage = "Unable to describe the topic " + topicName;
        log.error(errorMessage, ex);
        throw new Exception(errorMessage, ex);
      }
    }
  }

  public List<OffsetDetails> getConsumerGroupDetails(
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

    List<OffsetDetails> consumerGroupOffsetList = new ArrayList<>();
    OffsetDetails offsetDetails;
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

        offsetDetails = new OffsetDetails();
        long earliestOffset =
            listOffsetsEarliestResult.partitionResult(topicPartition).get().offset();
        long latestOffset = listOffsetsLatestResult.partitionResult(topicPartition).get().offset();
        long lag = latestOffset - earliestOffset;

        offsetDetails.setTopicPartitionId(Long.toString(topicPartition.partition()));
        offsetDetails.setCurrentOffset(Long.toString(earliestOffset));
        offsetDetails.setEndOffset(Long.toString(latestOffset));
        offsetDetails.setLag(Long.toString(lag));
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
