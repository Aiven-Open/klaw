import {
  TopicAdvancedConfigurationOptions,
  TopicApiResponse,
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
    text: "",
  },
  "compression.type": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_compression.type",
    text: "",
  },
  "delete.retention.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_delete.retention.ms",
    text: "",
  },
  "file.delete.delay.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_file.delete.delay.ms",
    text: "",
  },
  "flush.messages": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_flush.messages",
    text: "",
  },
  "flush.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_flush.ms",
    text: "",
  },
  "follower.replication.throttled.replicas": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_follower.replication.throttled.replicas",
    text: "",
  },
  "index.interval.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_index.interval.bytes",
    text: "",
  },
  "leader.replication.throttled.replicas": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_leader.replication.throttled.replicas",
    text: "",
  },
  "max.compaction.lag.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_max.compaction.lag.ms",
    text: "",
  },
  "max.message.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_max.message.bytes",
    text: "",
  },
  "message.downconversion.enable": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.downconversion.enable",
    text: "",
  },
  "message.format.version": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.format.version",
    text: "",
  },
  "message.timestamp.difference.max.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.timestamp.difference.max.ms",
    text: "",
  },
  "message.timestamp.type": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_message.timestamp.type",
    text: "",
  },
  "min.cleanable.dirty.ratio": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_min.cleanable.dirty.ratio",
    text: "",
  },
  "min.compaction.lag.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_min.compaction.lag.ms",
    text: "",
  },
  "min.insync.replicas": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_min.insync.replicas",
    text: "",
  },
  preallocate: {
    link: "https://kafka.apache.org/documentation/#topicconfigs_preallocate",
    text: "",
  },
  "retention.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_retention.bytes",
    text: "",
  },
  "retention.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_retention.ms",
    text: "",
  },
  "segment.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.bytes",
    text: "",
  },
  "segment.index.bytes": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.index.bytes",
    text: "",
  },
  "segment.jitter.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.jitter.ms",
    text: "",
  },
  "segment.ms": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_segment.ms",
    text: "",
  },
  "unclean.leader.election.enable": {
    link: "https://kafka.apache.org/documentation/#topicconfigs_unclean.leader.election.enable",
    text: "",
  },
};

function transformgetTopicAdvancedConfigOptionsResponse(
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

export {
  transformTopicApiResponse,
  transformgetTopicAdvancedConfigOptionsResponse,
};
