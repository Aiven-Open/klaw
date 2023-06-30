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
import { MarkdownString } from "src/domain/helper/documentation-helper";

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

type TopicDocumentationMarkdown = MarkdownString;

// KlawApiModel<"TopicOverview">
// Represents the TopicOverview as defined in the backend.
// "TopicOverview" is the type (and object) we're using in FE
// we're redefining property types here to fit our need in app better
// transformTopicOverviewResponse() is taking care of transforming
// the properties and makes sure the types are matching between BE and FE
type TopicOverview = ResolveIntersectionTypes<
  Omit<
    KlawApiModel<"TopicOverview">,
    "topicInfoList" | "topicDocumentation"
  > & {
    // "topicInfoList" is a list of KlawApiModel<"TopicOverviewInfo">
    // there is only ever one entry in this list, and we want to access
    // it accordingly, that's why we transform it to topicInfo instead
    topicInfo: KlawApiModel<"TopicOverviewInfo">;
    topicDocumentation?: TopicDocumentationMarkdown;
  }
>;

type AclOverviewInfo = KlawApiModel<"AclOverviewInfo">;

type TopicSchemaOverview = KlawApiModel<"SchemaOverview">;

// "remark" is currently not implemented in the API
// and will be added later. We're already preparing
// our UI and code for that.
type DeleteTopicPayload = ResolveIntersectionTypes<
  KlawApiModel<"TopicDeleteRequestModel"> & {
    remark?: string;
  }
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
  AclOverviewInfo,
  TopicMessages,
  NoContent,
  TopicOverview,
  TopicSchemaOverview,
  DeleteTopicPayload,
  TopicDocumentationMarkdown,
};
