import {
  getTopicNames,
  getTopics,
  getTopicTeam,
} from "src/domain/topic/topic-api";
import {
  Topic,
  TopicNames,
  TopicRequestTypes,
  TopicTeam,
  TopicRequestStatus,
  TopicRequest,
} from "src/domain/topic/topic-types";

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicRequest,
  TopicRequestTypes,
  TopicRequestStatus,
};
export { getTopics, getTopicNames, getTopicTeam };
