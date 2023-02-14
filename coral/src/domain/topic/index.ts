import {
  getTopicNames,
  getTopics,
  getTopicTeam,
} from "src/domain/topic/topic-api";
import {
  Topic,
  TopicNames,
  TopicRequest,
  TopicRequestTypes,
  TopicTeam,
  TopicRequestStatus,
  TopicRequestNew,
} from "src/domain/topic/topic-types";

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicRequest,
  TopicRequestNew,
  TopicRequestTypes,
  TopicRequestStatus,
};
export { getTopics, getTopicNames, getTopicTeam };
