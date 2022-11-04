package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.time.Duration;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TopicContentsService {

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
      String clusterName,
      String consumerGroupId,
      String topicName,
      int offsetPosition,
      String readMessagesType) {
    log.info(
        "readEvents bootStrapServers {}, protocol {}, clusterName {},  consumerGroupId {},  topicName {},\n"
            + "                                               offsetPosition {},  readMessagesType {}",
        bootStrapServers,
        protocol,
        clusterName,
        consumerGroupId,
        topicName,
        offsetPosition,
        readMessagesType);

    Map<Long, String> eventMap = new TreeMap<>();
    KafkaConsumer<String, String> consumer;

    if (consumerGroupId.equals("notdefined")) {
      consumer = getKafkaConsumer(kwGenericConsumerGroupId, bootStrapServers, protocol);
    } else {
      consumer = getKafkaConsumer(consumerGroupId, bootStrapServers, protocol);
    }

    consumer.subscribe(Collections.singleton(topicName));
    consumer.poll(Duration.ofMillis(defaultPollInterval));
    Set<TopicPartition> assignment = consumer.assignment();

    consumer.seekToBeginning(assignment);
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(assignment);
    long newOffset;
    if (readMessagesType.equals("OFFSET_ID")) {
      for (TopicPartition tp : assignment) {
        long beginningOffset = consumer.position(tp);
        long endOffset = endOffsets.get(tp);
        newOffset = endOffset - offsetPosition;
        if (newOffset < beginningOffset) {
          newOffset = beginningOffset;
        }

        consumer.seek(tp, newOffset);
      }
    }

    int i = 0;
    int numOfEventsToRead = 5;
    do {
      ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(500));
      consumerRecords.forEach(
          record -> {
            eventMap.put(record.offset(), record.value());
          });
      i++;
    } while (i != numOfEventsToRead);

    consumer.commitAsync();
    consumer.close();
    return eventMap;
  }

  public KafkaConsumer<String, String> getKafkaConsumer(
      String groupId, String bootstrapServers, String protocol) {
    Properties props = new Properties();

    if (protocol.equals("SSL")) {
      props = clusterApiUtils.getSslConfig("");
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
