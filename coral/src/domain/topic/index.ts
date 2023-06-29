import {
  getTopicNames,
  getTopics,
  getTopicTeam,
  deleteTopic,
  updateTopicDocumentation,
} from "src/domain/topic/topic-api";
import {
  Topic,
  TopicNames,
  TopicRequestOperationTypes,
  TopicTeam,
  TopicRequestStatus,
  TopicRequest,
  TopicOverview,
  DeleteTopicPayload,
} from "src/domain/topic/topic-types";

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicRequest,
  TopicRequestOperationTypes,
  TopicRequestStatus,
  TopicOverview,
  DeleteTopicPayload,
};
export {
  getTopics,
  getTopicNames,
  getTopicTeam,
  deleteTopic,
  updateTopicDocumentation,
};
