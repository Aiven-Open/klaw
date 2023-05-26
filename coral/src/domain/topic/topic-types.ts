import type {
  KlawApiModel,
  KlawApiResponse,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";
import {
  RequestStatus,
  RequestOperationType,
} from "src/domain/requests/requests-types";

type TopicApiResponse = ResolveIntersectionTypes<Paginated<Topic[]>>;

type Topic = KlawApiModel<"TopicInfo">;
type TopicNames = KlawApiResponse<"getTopicsOnly">;
type TopicTeam = KlawApiModel<"TopicTeamResponse">;
type TopicMessages = {
  [key: string]: string | undefined;
};
type NoContent = {
  status: boolean;
};

type TopicAdvancedConfigurationOptions = {
  key: string;
  name?: string;
  documentation?: {
    link: string;
    text: string;
  };
};

type TopicRequestOperationTypes = RequestOperationType;
type TopicRequestStatus = RequestStatus;

type TopicRequest = KlawApiModel<"TopicRequestsResponseModel">;

type TopicRequestApiResponse = ResolveIntersectionTypes<
  Paginated<TopicRequest[]>
>;

type AclOverviewInfo = KlawApiModel<"AclOverviewInfo">;
type TopicOverviewApiResponse = KlawApiResponse<"getTopicOverview">;

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
  TopicOverviewApiResponse,
  AclOverviewInfo,
  TopicMessages,
  NoContent,
};
