import {
  TopicAdvancedConfigurationOptions,
  TopicApiResponse,
  TopicRequestApiResponse,
} from "src/domain/topic/topic-types";
import { KlawApiResponse } from "types/utils";

// @TODO check zod for this!
function transformTopicApiResponse(
  apiResponse: KlawApiResponse<"topicsGet">
): TopicApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0][0].totalNoPages),
    currentPage: Number(apiResponse[0][0].currentPage),
    entries: apiResponse.flat(),
  };
}

const ADVANCED_TOPIC_CONFIG_DOCUMENTATION: Record<
  string,
  { link: string; text: string }
> = {
  "cleanup.policy": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_cleanup.policy",
    text: "Specify the cleanup policy for log segments in a topic.",
  },
  "compression.type": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_compression.type",
    text: "Specify the type of compression used for log segments in a topic.",
  },
  "delete.retention.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_delete.retention.ms",
    text: "Specify retention time in milliseconds for the delete tombstone markers for log compacted topics.",
  },
  "file.delete.delay.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_file.delete.delay.ms",
    text: "Specify the wait time in milliseconds before deleting a file from the filesystem.",
  },
  "flush.messages": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_flush.messages",
    text: "Specify the number of messages that must be written to a topic before a flush is forced.",
  },
  "flush.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_flush.ms",
    text: "Specify the wait time in milliseconds before forcing a flush of data.",
  },
  "follower.replication.throttled.replicas": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_follower.replication.throttled.replicas",
    text: "Specify the list of replicas that are currently throttled for replication for this topic.",
  },
  "index.interval.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_index.interval.bytes",
    text: "Specify the interval at which log file offsets will be indexed.",
  },
  "leader.replication.throttled.replicas": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_leader.replication.throttled.replicas",
    text: "Specify replicas for which log replication should be throttled on the leader side.",
  },
  "max.compaction.lag.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_max.compaction.lag.ms",
    text: "Specify maximum time a message will remain ineligible for compaction in the log.",
  },
  "max.message.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_max.message.bytes",
    text: "Specify the maximum size in bytes for a batch.",
  },
  "message.downconversion.enable": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.downconversion.enable",
    text: "Enable or disable automatic down conversion of messages.",
  },
  "message.format.version": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.format.version",
    text: "Specify the message format version to be used by the broker to append messages to logs.",
  },
  "message.timestamp.difference.max.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.timestamp.difference.max.ms",
    text: "Specify the maximum time difference in milliseconds allowed between the timestamp of a message and time received.",
  },
  "message.timestamp.type": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.timestamp.type",
    text: "Specify if `CreateTime` or `LogAppendTime` should be used as the timestamp of the message.",
  },
  "min.cleanable.dirty.ratio": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_min.cleanable.dirty.ratio",
    text: "Specify the ratio of log to retention size to initiate log compaction.",
  },
  "min.compaction.lag.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_min.compaction.lag.ms",
    text: "Specify the minimum time a message will remain uncompacted in the log.",
  },
  "min.insync.replicas": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_min.insync.replicas",
    text: "Specify the minimum number of replicas required for a write to be considered successful.",
  },
  preallocate: {
    link: "https://kafka.apache.org/documentation/#topicconfigs_preallocate",
    text: "Enable or disable preallocation of file disk for a new log segment.",
  },
  "retention.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_retention.bytes",
    text: "Specify the maximum size a partition before log segment are discarded to free up space.",
  },
  "retention.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_retention.ms",
    text: "Specify the retention period in milliseconds for logs before discarding it to free up space.",
  },
  "segment.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.bytes",
    text: "Specify the maximum size of a log segment file in bytes.",
  },
  "segment.index.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.index.bytes",
    text: "Specify the maximum size of the index in bytes that maps offsets to file positions.",
  },
  "segment.jitter.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.jitter.ms",
    text: "Specify the maximum jitter time in milliseconds.",
  },
  "segment.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.ms",
    text: "Specify the maximum time in milliseconds before a log segment is rolled.",
  },
  "unclean.leader.election.enable": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_unclean.leader.election.enable",
    text: "Enable or disable unclean leader election.",
  },
};

function transformGetTopicAdvancedConfigOptionsResponse(
  apiResponse: KlawApiResponse<"topicAdvancedConfigGet">
): TopicAdvancedConfigurationOptions[] {
  return Object.entries(apiResponse).map(([key, name]) => {
    const base = { key, name };
    if (name in ADVANCED_TOPIC_CONFIG_DOCUMENTATION) {
      return {
        ...base,
        documentation: ADVANCED_TOPIC_CONFIG_DOCUMENTATION[name],
      };
    }
    return base;
  });
}

function transformGetTopicRequestsForApproverResponse(
  apiResponse: KlawApiResponse<"getTopicRequestsForApprover">
): TopicRequestApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    currentPage: Number(apiResponse[0].currentPage),
    entries: apiResponse,
  };
}

export {
  transformTopicApiResponse,
  transformGetTopicAdvancedConfigOptionsResponse,
  transformGetTopicRequestsForApproverResponse,
};
