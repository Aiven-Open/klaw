package io.aiven.klaw.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TopicConfiguration {
  CLEANUP_POLICY("cleanup.policy"),
  COMPRESSION_TYPE("compression.type"),
  DELETE_RETENTION_MS("delete.retention.ms"),
  FILE_DELETE_DELAY_MS("file.delete.delay.ms"),
  FLUSH_MESSAGES("flush.messages"),
  FLUSH_MS("flush.ms"),
  FOLLOWER_REPLICATION_THROTTLED_REPLICAS("follower.replication.throttled.replicas"),
  INDEX_INTERVAL_BYTES("index.interval.bytes"),
  LEADER_REPLICATION_THROTTLED_REPLICAS("leader.replication.throttled.replicas"),
  MAX_COMPACTION_LAG_MS("max.compaction.lag.ms"),
  MAX_MESSAGE_BYTES("max.message.bytes"),
  MESSAGE_FORMAT_VERSION("message.format.version"),
  MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS("message.timestamp.difference.max.ms"),
  MESSAGE_TIMESTAMP_TYPE("message.timestamp.type"),
  MIN_CLEANABLE_DIRTY_RATIO("min.cleanable.dirty.ratio"),
  MIN_COMPACTION_LAG_MS("min.compaction.lag.ms"),
  MIN_INSYNC_REPLICAS("min.insync.replicas"),
  PREALLOCATE("preallocate"),
  RETENTION_BYTES("retention.bytes"),
  RETENTION_MS("retention.ms"),
  SEGMENT_BYTES("segment.bytes"),
  SEGMENT_INDEX_BYTES("segment.index.bytes"),
  SEGMENT_JITTER_MS("segment.jitter.ms"),
  SEGMENT_MS("segment.ms"),
  UNCLEAN_LEADER_ELECTION_ENABLE("unclean.leader.election.enable"),
  MESSAGE_DOWNCONVERSION_ENABLE("message.downconversion.enable");

  final String value;

  TopicConfiguration(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  public static Map<String, String> getTopicConfigurations() {
    Map<String, String> topicConfigs = new HashMap<>();
    for (TopicConfiguration topicConfiguration : TopicConfiguration.values()) {
      topicConfigs.put(topicConfiguration.name(), topicConfiguration.getValue());
    }
    return Collections.unmodifiableMap(topicConfigs);
  }
}
