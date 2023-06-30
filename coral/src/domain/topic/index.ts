import {
  deleteTopic,
  getTopicDetailsPerEnv,
  getTopicNames,
  getTopicRequests,
  getTopicTeam,
  getTopics,
  promoteTopic,
  updateTopicDocumentation,
} from "src/domain/topic/topic-api";
import {
  DeleteTopicPayload,
  Topic,
  TopicDetailsPerEnv,
  TopicDocumentationMarkdown,
  TopicNames,
  TopicOverview,
  TopicRequest,
  TopicRequestOperationTypes,
  TopicRequestStatus,
  TopicSchemaOverview,
  TopicTeam,
} from "src/domain/topic/topic-types";

export {
  deleteTopic,
  getTopicDetailsPerEnv,
  getTopicNames,
  getTopicRequests,
  getTopicTeam,
  getTopics,
  promoteTopic,
  updateTopicDocumentation,
};
export type {
  DeleteTopicPayload,
  Topic,
  TopicDetailsPerEnv,
  TopicDocumentationMarkdown,
  TopicNames,
  TopicOverview,
  TopicRequest,
  TopicRequestOperationTypes,
  TopicRequestStatus,
  TopicSchemaOverview,
  TopicTeam,
};
