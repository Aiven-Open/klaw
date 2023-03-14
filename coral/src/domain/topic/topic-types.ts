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
// Same as below, response is defined inline as string[] for getTopicsOnly
// We did this https://github.com/aiven/klaw/blob/main/openapi.yaml#L751-L755
type TopicNames = ResolveIntersectionTypes<
  KlawApiModel<"TopicsGetOnlyResponse">
>;
// The TopicGetTeamResponse model does not exist,
// the response is defined inline as { [key: string]: string }
// for the getTopicTeam endpoint
// We used to have this component schema : https://github.com/aiven/klaw/blob/main/openapi.yaml#L756-L762
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

type TopicRequest = ResolveIntersectionTypes<KlawApiModel<"TopicRequestModel">>;

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
