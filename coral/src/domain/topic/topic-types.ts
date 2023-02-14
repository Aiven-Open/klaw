import type { KlawApiModel, ResolveIntersectionTypes } from "types/utils";
import { components } from "types/api";

type Paginated<T> = {
  totalPages: number;
  currentPage: number;
  entries: T;
};

type TopicApiResponse = Paginated<Topic[]>;

type Topic = ResolveIntersectionTypes<KlawApiModel<"TopicInfo">>;
type TopicNames = ResolveIntersectionTypes<
  KlawApiModel<"TopicsGetOnlyResponse">
>;
type TopicTeam = ResolveIntersectionTypes<KlawApiModel<"TopicGetTeamResponse">>;

type TopicAdvancedConfigurationOptions = {
  key: string;
  name: string;
  documentation?: {
    link: string;
    text: string;
  };
};

// @TODO add more refined typing when implementation needs are more clear
type TopicRequestTypes = components["schemas"]["TopicRequestTypes"];
type TopicRequestStatus = components["schemas"]["RequestStatus"];

type TopicRequestNew = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"TopicRequest">,
      | "topicid"
      | "topicname"
      | "environmentName"
      | "topictype"
      | "teamname"
      | "requestor"
      | "requesttimestring"
    >
  > &
    KlawApiModel<"TopicRequest">
>;

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
  TopicRequestNew,
  TopicRequestTypes,
  TopicRequestStatus,
};
