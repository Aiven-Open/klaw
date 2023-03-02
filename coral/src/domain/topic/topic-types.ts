import type {
  KlawApiModel,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";
import {
  RequestStatus,
  RequestOperationType,
} from "src/domain/requests/requests-types";

type TopicApiResponse = ResolveIntersectionTypes<Paginated<Topic[]>>;

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

type TopicRequestOperationTypes = RequestOperationType;
type TopicRequestStatus = RequestStatus;

type TopicRequest = ResolveIntersectionTypes<KlawApiModel<"TopicRequest">>;

type TopicRequestApiResponse = ResolveIntersectionTypes<
  Paginated<TopicRequest[]>
>;

export type {
  Topic,
  TopicNames,
  TopicTeam,
  TopicApiResponse,
  TopicAdvancedConfigurationOptions,
  TopicRequest,
  TopicRequestOperationTypes,
  TopicRequestStatus,
  TopicRequestApiResponse,
};
