import {
  getTopicNames,
  getTopics,
  getTopicTeam,
  deleteTopic,
} from "src/domain/topic/topic-api";
import {
  Topic,
  TopicNames,
  TopicRequestOperationTypes,
  TopicTeam,
  TopicRequestStatus,
  TopicRequest,
  TopicOverview,
} from "src/domain/topic/topic-types";

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicRequest,
  TopicRequestOperationTypes,
  TopicRequestStatus,
  TopicOverview,
};
export { getTopics, getTopicNames, getTopicTeam, deleteTopic };
