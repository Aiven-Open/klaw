package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.enums.TopicContentType;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.time.Duration;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TopicContentsService {

  public static final int RANGE_MAX_RECORDS = 100;
  public static final int NUMBER_OF_POLLS = 3;
  final ClusterApiUtils clusterApiUtils;

  @Value("${klaw.topiccontents.consumergroup.id:notdefined}")
  private String kwGenericConsumerGroupId;

  @Value("${klaw.topiccontents.consumer.poll.interval.ms:2000}")
  private long defaultPollInterval;

  public TopicContentsService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public Map<Long, String> readEvents(
      String bootStrapServers,
      String protocol,
      String consumerGroupId,
      String topicName,
      String offsetPosition,
      Integer selectedPartitionId,
      Integer selectedNumberOfOffsets,
      Integer rangeOffsetsStart,
      Integer rangeOffsetsEnd,
      String readMessagesType,
      String clusterIdentification) {
    log.debug(
        "readEvents bootStrapServers {}, protocol {},  consumerGroupId {},"
            + " topicName {}, offsetPosition {},  readMessagesType {} clusterIdentification {} selectedPartitionId {}"
            + " selectedNumberOfOffsets {} rangeOffsetsStart {} rangeOffsetsEnd {}",
        bootStrapServers,
        protocol,
        consumerGroupId,
        topicName,
        offsetPosition,
        readMessagesType,
        clusterIdentification,
        selectedPartitionId,
        selectedNumberOfOffsets,
        rangeOffsetsStart,
        rangeOffsetsEnd);

    Map<Long, String> eventMap = new TreeMap<>();
    KafkaConsumer<String, String> consumer;

    if (offsetPosition.equals(TopicContentType.RANGE.getValue())
        && (rangeOffsetsStart < 0 || rangeOffsetsEnd < 0)) {
      return eventMap;
    }

    if (consumerGroupId.equals("notdefined")) {
      consumer =
          getKafkaConsumer(
              kwGenericConsumerGroupId, bootStrapServers, protocol, clusterIdentification);
    } else {
      consumer =
          getKafkaConsumer(consumerGroupId, bootStrapServers, protocol, clusterIdentification);
    }

    consumer.subscribe(Collections.singleton(topicName));
    consumer.poll(Duration.ofMillis(defaultPollInterval));
    Set<TopicPartition> topicPartitionsSet = consumer.assignment();

    Set<TopicPartition> partitionsAssignment = new HashSet<>();
    if (offsetPosition.equals(TopicContentType.CUSTOM.getValue())
        || offsetPosition.equals(TopicContentType.RANGE.getValue())) {
      for (TopicPartition tp : topicPartitionsSet) {
        if (tp.partition() == selectedPartitionId) {
          partitionsAssignment = Collections.singleton(tp);
          break;
        }
      }
    } else {
      partitionsAssignment = topicPartitionsSet;
    }

    if (partitionsAssignment.isEmpty()
        || (offsetPosition.equals(TopicContentType.RANGE.getValue())
            && rangeOffsetsStart > rangeOffsetsEnd)) {
      consumer.close();
      return eventMap;
    }
    consumer.seekToBeginning(partitionsAssignment);
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitionsAssignment);
    long newOffset;
    if (readMessagesType.equals("OFFSET_ID")) {
      for (TopicPartition tp : partitionsAssignment) {
        long beginningOffset = consumer.position(tp);
        long endOffset = endOffsets.get(tp);
        if (offsetPosition.equals(TopicContentType.CUSTOM.getValue())) {
          newOffset = endOffset - selectedNumberOfOffsets;
        } else if (offsetPosition.equals(TopicContentType.RANGE.getValue())) {
          newOffset = rangeOffsetsStart;
        } else {
          newOffset = endOffset - Integer.parseInt(offsetPosition);
        }
        if (newOffset < beginningOffset) {
          newOffset = beginningOffset;
        }

        consumer.seek(tp, newOffset);
      }
    }

    int i = 0;
    boolean exitLoop = false;
    do {
      ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(500));
      for (ConsumerRecord<String, String> record : consumerRecords) {
        eventMap.put(record.offset(), record.value());
        if (offsetPosition.equals(TopicContentType.RANGE.getValue())
            && (record.offset() >= rangeOffsetsEnd || eventMap.size() >= RANGE_MAX_RECORDS)) {
          exitLoop = true;
          break;
        }
      }
      i++;
    } while (i != NUMBER_OF_POLLS && !exitLoop);

    consumer.commitAsync();
    consumer.close();
    return eventMap;
  }

  public KafkaConsumer<String, String> getKafkaConsumer(
      String groupId, String bootstrapServers, String protocol, String clusterIdentification) {
    Properties props = new Properties();

    if (protocol.equals("SSL")) {
      props = clusterApiUtils.getSslConfig(clusterIdentification);
      props.put("security.protocol", "SSL");
    }
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "KLAW_TMP_GROUP_CLIENT" + groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    return new KafkaConsumer<>(props);
  }
}
