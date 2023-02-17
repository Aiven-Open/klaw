import type {
  KlawApiModel,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";
import { RequestStatus, RequestType } from "src/domain/requests";

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

type TopicRequestTypes = RequestType;
type TopicRequestStatus = RequestStatus;

type TopicRequest = ResolveIntersectionTypes<
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
  TopicRequestTypes,
  TopicRequestStatus,
  TopicRequestApiResponse,
};
