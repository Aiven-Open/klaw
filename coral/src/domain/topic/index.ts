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
  TopicDocumentationMarkdown,
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
  TopicDocumentationMarkdown,
};
export {
  getTopics,
  getTopicNames,
  getTopicTeam,
  deleteTopic,
  updateTopicDocumentation,
};
