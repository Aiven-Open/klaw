import omitBy from "lodash/omitBy";
import { Schema } from "src/app/features/topics/request/form-schemas/topic-request-form";
import { transformAdvancedConfigEntries } from "src/app/features/topics/request/utils";
import {
  StringifiedHtml,
  createStringifiedHtml,
} from "src/domain/helper/documentation-helper";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests/requests-types";
import {
  transformGetTopicAdvancedConfigOptionsResponse,
  transformGetTopicRequestsResponse,
  transformTopicApiResponse,
  transformTopicOverviewResponse,
} from "src/domain/topic/topic-transformer";
import {
  DeleteTopicPayload,
  NoContent,
  TopicAdvancedConfigurationOptions,
  TopicApiResponse,
  TopicClaimPayload,
  TopicDocumentationMarkdown,
  TopicMessages,
  TopicOverview,
  TopicRequestApiResponse,
} from "src/domain/topic/topic-types";
import api, { API_PATHS } from "src/services/api";
import { convertQueryValuesToString } from "src/services/api-helper";
import {
  KlawApiModel,
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
} from "types/utils";

const getTopics = async (
  params: KlawApiRequestQueryParameters<"getTopics">
): Promise<TopicApiResponse> => {
  const queryParams = convertQueryValuesToString({
    pageNo: params.pageNo,
    env: params.env,
    ...(params.teamId && { teamId: params.teamId }),
    ...(params.topicnamesearch && {
      topicnamesearch: params.topicnamesearch,
    }),
  });

  return api
    .get<KlawApiResponse<"getTopics">>(
      API_PATHS.getTopics,
      new URLSearchParams(queryParams)
    )
    .then(transformTopicApiResponse);
};

type GetTopicNamesArgs = Partial<{
  onlyMyTeamTopics: boolean;
  envSelected?: string;
}>;

const getTopicNames = async ({
  onlyMyTeamTopics,
  envSelected = "ALL",
}: GetTopicNamesArgs = {}) => {
  const isMyTeamTopics = onlyMyTeamTopics ?? false;
  const params = {
    isMyTeamTopics: isMyTeamTopics.toString(),
    envSelected,
  };

  return api.get<KlawApiResponse<"getTopicsOnly">>(
    API_PATHS.getTopicsOnly,
    new URLSearchParams(params)
  );
};

interface GetTopicTeamArgs {
  topicName: string;
  patternType?: "LITERAL" | "PREFIXED";
}

const getTopicTeam = async ({
  topicName,
  patternType = "LITERAL",
}: GetTopicTeamArgs) => {
  const params = { topicName, patternType };

  return api.get<KlawApiResponse<"getTopicTeam">>(
    API_PATHS.getTopicTeam,
    new URLSearchParams(params)
  );
};

const getTopicAdvancedConfigOptions = (): Promise<
  TopicAdvancedConfigurationOptions[]
> =>
  api
    .get<KlawApiResponse<"getAdvancedTopicConfigs">>(
      API_PATHS.getAdvancedTopicConfigs
    )
    .then(transformGetTopicAdvancedConfigOptionsResponse);

const requestTopicCreation = (
  data: Schema
): Promise<KlawApiResponse<"createTopicsCreateRequest">> => {
  const payload: KlawApiRequest<"createTopicsCreateRequest"> = {
    description: data.description,
    environment: data.environment.id,
    remarks: data.remarks,
    topicname: data.topicname,
    replicationfactor: data.replicationfactor,
    topicpartitions: parseInt(data.topicpartitions, 10),
    advancedTopicConfigEntries: transformAdvancedConfigEntries(
      data.advancedConfiguration
    ),
    requestOperationType: "CREATE",
  };
  return api.post<
    KlawApiResponse<"createTopicsCreateRequest">,
    KlawApiRequest<"createTopicsCreateRequest">
  >(API_PATHS.createTopicsCreateRequest, payload);
};

const requestTopicPromotion = (
  data: Schema
): Promise<KlawApiResponse<"createTopicsCreateRequest">> => {
  const payload: KlawApiRequest<"createTopicsCreateRequest"> = {
    description: data.description,
    environment: data.environment.id,
    remarks: data.remarks,
    topicname: data.topicname,
    replicationfactor: data.replicationfactor,
    topicpartitions: parseInt(data.topicpartitions, 10),
    advancedTopicConfigEntries: transformAdvancedConfigEntries(
      data.advancedConfiguration
    ),
    requestOperationType: "PROMOTE",
  };
  return api.post<
    KlawApiResponse<"createTopicsCreateRequest">,
    KlawApiRequest<"createTopicsCreateRequest">
  >(API_PATHS.createTopicsCreateRequest, payload);
};

type RequestTopicEdit = Schema & { topicId: string };
// @TODO this should use the createTopicsCreateRequest`endpoint, but it does not handle the UPDATE type yet
// Update when backend changes are merged to handle UPDATE with createTopicsCreateRequest
// @TODO 2: when creating a request to update a topic, we need to set the topicId in the optional
// 'otherParams'. We will update the endpoint(s) to be better named and more precise (not having an
// important param be optional), that's why we're adding a bit of a messy fix here instead of updating
// forms schema etc.
const requestTopicEdit = (
  data: RequestTopicEdit
): Promise<KlawApiResponse<"createTopicsUpdateRequest">> => {
  const payload: KlawApiRequest<"createTopicsUpdateRequest"> = {
    description: data.description,
    environment: data.environment.id,
    remarks: data.remarks,
    topicname: data.topicname,
    replicationfactor: data.replicationfactor,
    topicpartitions: parseInt(data.topicpartitions, 10),
    advancedTopicConfigEntries: transformAdvancedConfigEntries(
      data.advancedConfiguration
    ),
    requestOperationType: "UPDATE",
    otherParams: data.topicId,
  };
  return api.post<
    KlawApiResponse<"createTopicsUpdateRequest">,
    KlawApiRequest<"createTopicsUpdateRequest">
  >(API_PATHS.createTopicsUpdateRequest, payload);
};

const getTopicRequestsForApprover = (
  params: KlawApiRequestQueryParameters<"getTopicRequestsForApprover">
): Promise<TopicRequestApiResponse> => {
  const filteredParams = omitBy(
    { ...params, teamId: String(params.teamId) },
    (value, property) => {
      const omitTeamId = property === "teamId" && value === "undefined";
      const omitSearch = property === "search" && value === "";
      const omitEnv =
        property === "env" && (value === "ALL" || value === undefined);
      const omitOperationType =
        property === "operationType" && value === undefined;

      return omitTeamId || omitSearch || omitEnv || omitOperationType;
    }
  );

  return api
    .get<KlawApiResponse<"getTopicRequestsForApprover">>(
      API_PATHS.getTopicRequestsForApprover,
      new URLSearchParams(filteredParams)
    )
    .then(transformGetTopicRequestsResponse);
};

const getTopicRequests = (
  params: KlawApiRequestQueryParameters<"getTopicRequests">
): Promise<TopicRequestApiResponse> => {
  const filteredParams = omitBy(
    { ...params, isMyRequest: String(Boolean(params.isMyRequest)) },
    (value, property) => {
      const omitIsMyRequest = property === "isMyRequest" && value !== "true"; // Omit if anything else than true
      const omitSearch =
        property === "search" && (value === "" || value === undefined);
      const omitEnv =
        property === "env" && (value === "ALL" || value === undefined);
      const omitRequestOperationType =
        property === "operationType" &&
        (value === "ALL" || value === undefined);

      return (
        omitIsMyRequest || omitSearch || omitEnv || omitRequestOperationType
      );
    }
  );

  return api
    .get<KlawApiResponse<"getTopicRequests">>(
      API_PATHS.getTopicRequests,
      new URLSearchParams(filteredParams)
    )
    .then(transformGetTopicRequestsResponse);
};

const getTopicMessages = (
  params: KlawApiRequestQueryParameters<"getTopicEvents">
): Promise<TopicMessages | NoContent> => {
  return api.get<KlawApiResponse<"getTopicEvents">>(
    "/getTopicEvents",
    new URLSearchParams(params)
  );
};

const approveTopicRequest = ({
  reqIds,
}: {
  reqIds: RequestVerdictApproval<"SCHEMA">["reqIds"];
}) => {
  return api.post<
    KlawApiResponse<"approveRequest">,
    RequestVerdictApproval<"TOPIC">
  >(API_PATHS.approveRequest, {
    reqIds,
    requestEntityType: "TOPIC",
  });
};

const declineTopicRequest = ({
  reqIds,
  reason,
}: Omit<RequestVerdictDecline<"TOPIC">, "requestEntityType">) => {
  return api.post<
    KlawApiResponse<"declineRequest">,
    RequestVerdictDecline<"TOPIC">
  >(API_PATHS.declineRequest, {
    reqIds,
    reason,
    requestEntityType: "TOPIC",
  });
};

const deleteTopicRequest = ({
  reqIds,
}: Omit<RequestVerdictDelete<"TOPIC">, "requestEntityType">) => {
  return api.post<
    KlawApiResponse<"deleteRequest">,
    RequestVerdictDelete<"TOPIC">
  >(API_PATHS.deleteRequest, {
    reqIds,
    requestEntityType: "TOPIC",
  });
};

const requestTopicDeletion = (params: DeleteTopicPayload) => {
  // DeleteTopicPayload represents the KlawApiModel<"TopicDeleteRequestModel">
  // with "remark" added. "remark" is currently not implemented in the API
  // and will be added later. We 're already preparing
  // our UI and code for that.
  const payload: KlawApiModel<"TopicDeleteRequestModel"> = {
    deleteAssociatedSchema: params.deleteAssociatedSchema,
    env: params.env,
    topicName: params.topicName,
  };

  return api.post<
    KlawApiResponse<"createTopicDeleteRequest">,
    KlawApiModel<"TopicDeleteRequestModel">
  >(API_PATHS.createTopicDeleteRequest, payload);
};

const getTopicOverview = ({
  topicName,
  environmentId,
  groupBy,
}: KlawApiRequestQueryParameters<"getTopicOverview">): Promise<TopicOverview> => {
  const queryParams = convertQueryValuesToString({
    topicName,
    ...(environmentId && { environmentId }),
    ...(groupBy && {
      groupBy,
    }),
  });

  return api
    .get<KlawApiResponse<"getTopicOverview">>(
      API_PATHS.getTopicOverview,
      new URLSearchParams(queryParams)
    )
    .then(transformTopicOverviewResponse);
};

const getSchemaOfTopic = (
  params: KlawApiRequestQueryParameters<"getSchemaOfTopic">
) => {
  return api.get<KlawApiResponse<"getSchemaOfTopic">>(
    API_PATHS.getSchemaOfTopic,
    new URLSearchParams(convertQueryValuesToString(params))
  );
};

type UpdateTopicDocumentation = {
  topicName: string;
  topicIdForDocumentation: number;
  topicDocumentation: TopicDocumentationMarkdown;
};
async function updateTopicDocumentation({
  topicName,
  topicIdForDocumentation,
  topicDocumentation,
}: UpdateTopicDocumentation) {
  const stringifiedHtml = await createStringifiedHtml(topicDocumentation);

  // @ TODO
  // KlawApiRequest<"saveTopicDocumentation"> currently
  // lists too many props as required, only the three we're
  // passing are needed and used in Angular, too.
  // BE is working on that. We still need to use our
  // own typing for stringifiedHtml to make sure we
  // get a certain type safety up until this point
  const requestBody: {
    topicName: string;
    topicid: number;
    documentation: StringifiedHtml;
  } = {
    topicName,
    topicid: topicIdForDocumentation,
    documentation: stringifiedHtml,
  };

  return api.post<
    KlawApiResponse<"saveTopicDocumentation">,
    KlawApiRequest<"saveTopicDocumentation">
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
  >(API_PATHS.saveTopicDocumentation, requestBody);
}
const getTopicDetailsPerEnv = (
  params: KlawApiRequestQueryParameters<"getTopicDetailsPerEnv">
) => {
  return api.get<KlawApiResponse<"getTopicDetailsPerEnv">>(
    API_PATHS.getTopicDetailsPerEnv,
    new URLSearchParams(params)
  );
};

const requestTopicClaim = (params: TopicClaimPayload) => {
  const payload: KlawApiModel<"TopicClaimRequestModel"> = {
    env: params.env,
    topicName: params.topicName,
  };

  return api.post<
    KlawApiResponse<"createClaimTopicRequest">,
    KlawApiModel<"TopicClaimRequestModel">
  >(API_PATHS.createClaimTopicRequest, payload);
};

export {
  approveTopicRequest,
  requestTopicClaim,
  declineTopicRequest,
  requestTopicDeletion,
  deleteTopicRequest,
  requestTopicEdit,
  getSchemaOfTopic,
  getTopicAdvancedConfigOptions,
  getTopicDetailsPerEnv,
  getTopicMessages,
  getTopicNames,
  getTopicOverview,
  getTopicRequests,
  getTopicRequestsForApprover,
  getTopicTeam,
  getTopics,
  requestTopicPromotion,
  requestTopicCreation,
  updateTopicDocumentation,
};
