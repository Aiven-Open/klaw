import type { KlawApiModel } from "types/utils";

type Paginated<T> = {
  totalPages: number;
  currentPage: number;
  entries: T;
};

type TopicApiResponse = Paginated<Topic[]>;

type Topic = KlawApiModel<"TopicInfo">;
type TopicNames = KlawApiModel<"TopicsGetOnlyResponse">;
type TopicTeam = KlawApiModel<"TopicGetTeamResponse">;

type TopicAdvancedConfigurationOptions = {
  key: string;
  name: string;
  documentation?: {
    link: string;
    text: string;
  };
};

// The proper type for this will take shape once we know what data
// we need in the upcoming features.
type TopicRequest = {
  topicName: KlawApiModel<"TopicRequest">["topicname"];
};

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicApiResponse,
  TopicAdvancedConfigurationOptions,
  TopicRequest,
};
