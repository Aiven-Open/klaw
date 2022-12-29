import {
  getTopicNames,
  getTopics,
  getTopicTeam,
} from "src/domain/topic/topic-api";
import { Topic, TopicNames, TopicTeam } from "src/domain/topic/topic-types";

export type { Topic, TopicNames, TopicTeam };
export { getTopics, getTopicNames, getTopicTeam };
