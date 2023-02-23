import {
  getTopicNames,
  getTopics,
  getTopicTeam,
} from "src/domain/topic/topic-api";
import {
  Topic,
  TopicNames,
  TopicRequestOperationTypes,
  TopicTeam,
  TopicRequestStatus,
  TopicRequest,
} from "src/domain/topic/topic-types";

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicRequest,
  TopicRequestOperationTypes,
  TopicRequestStatus,
};
export { getTopics, getTopicNames, getTopicTeam };
