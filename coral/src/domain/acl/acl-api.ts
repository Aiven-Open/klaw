import omitBy from "lodash/omitBy";
import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
  GetCreatedAclRequestForApproverParameters,
  GetCreatedAclRequestParameters,
} from "src/domain/acl/acl-types";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
  RequestVerdictDelete,
} from "src/domain/requests/requests-types";
import api, { API_PATHS } from "src/services/api";
import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
  ResolveIntersectionTypes,
} from "types/utils";

const createAclRequest = (
  aclPayload:
    | CreateAclRequestTopicTypeProducer
    | CreateAclRequestTopicTypeConsumer
): Promise<KlawApiResponse<"createAcl">> => {
  return api.post<KlawApiResponse<"createAcl">, KlawApiRequest<"createAcl">>(
    API_PATHS.createAcl,
    {
      ...aclPayload,
      requestOperationType: "CREATE",
    }
  );
};

const filterGetAclRequestParams = (params: GetCreatedAclRequestParameters) => {
  return omitBy(
    { ...params, isMyRequest: String(Boolean(params.isMyRequest)) },
    (value, property) => {
      const omitEnv = property === "env" && value === "ALL";
      const omitAclType = property === "aclType" && value === "ALL";
      const omitTopic = property === "topic" && value === "";
      const omitIsMyRequest = property === "isMyRequest" && value !== "true";
      const omitOperationType =
        property === "operationType" && value === undefined;
      const omitSearch =
        property === "search" && (value === "" || value === undefined);

      return (
        omitEnv ||
        omitAclType ||
        omitTopic ||
        omitOperationType ||
        omitIsMyRequest ||
        omitSearch
      );
    }
  );
};

const getAclRequestsForApprover = (
  params: GetCreatedAclRequestForApproverParameters
) => {
  const filteredParams = filterGetAclRequestParams(params);

  return api
    .get<KlawApiResponse<"getAclRequestsForApprover">>(
      API_PATHS.getAclRequestsForApprover,
      new URLSearchParams(filteredParams)
    )
    .then(transformAclRequestApiResponse);
};

const getAclRequests = (params: GetCreatedAclRequestParameters) => {
  const filteredParams = filterGetAclRequestParams(params);

  return api
    .get<KlawApiResponse<"getAclRequests">>(
      API_PATHS.getAclRequests,
      new URLSearchParams(filteredParams)
    )
    .then(transformAclRequestApiResponse);
};

type ApproveAclRequestPayload = RequestVerdictApproval<"ACL">;
type ApproveRequestParams = {
  reqIds: ApproveAclRequestPayload["reqIds"];
};
const approveAclRequest = ({ reqIds }: ApproveRequestParams) => {
  return api.post<KlawApiResponse<"approveRequest">, ApproveAclRequestPayload>(
    API_PATHS.approveRequest,
    { requestEntityType: "ACL", reqIds }
  );
};

type DeclineAclRequestPayload = RequestVerdictDecline<"ACL">;
type DeclineRequestParams = {
  reqIds: DeclineAclRequestPayload["reqIds"];
  reason: DeclineAclRequestPayload["reason"];
};
const declineAclRequest = ({ reqIds, reason }: DeclineRequestParams) => {
  return api.post<KlawApiResponse<"declineRequest">, DeclineAclRequestPayload>(
    API_PATHS.declineRequest,
    { requestEntityType: "ACL", reqIds, reason }
  );
};

type DeleteAclRequestPayload = RequestVerdictDelete<"ACL">;
type DeleteRequestParams = {
  reqIds: DeleteAclRequestPayload["reqIds"];
};
const deleteAclRequest = ({ reqIds }: DeleteRequestParams) => {
  return api.post<KlawApiResponse<"deleteRequest">, DeleteAclRequestPayload>(
    API_PATHS.deleteRequest,
    { requestEntityType: "ACL", reqIds }
  );
};

const requestAclDeletion = (
  payload: KlawApiRequest<"deleteAclSubscriptionRequest">
): Promise<KlawApiResponse<"deleteAclSubscriptionRequest">> => {
  return api.post<
    KlawApiResponse<"deleteAclSubscriptionRequest">,
    KlawApiRequest<"deleteAclSubscriptionRequest">
  >(API_PATHS.deleteAclSubscriptionRequest, payload);
};

type GetAivenServiceAccountsParams =
  KlawApiRequestQueryParameters<"getAivenServiceAccounts">;
type GetAivenServiceAccountsResponse =
  KlawApiResponse<"getAivenServiceAccounts">;
function getAivenServiceAccounts(
  params: GetAivenServiceAccountsParams
): Promise<GetAivenServiceAccountsResponse> {
  return api.get<GetAivenServiceAccountsResponse>(
    API_PATHS.getAivenServiceAccounts,
    new URLSearchParams(params)
  );
}

/*** The parameter "userName" that the endpoint expects is actually
 * the name of the service (acl_ssl). Since this is very confusing
 * and error-prone, we name it different for the params of our
 * api call function
 * Will be renamed in Backend after release 2.5.
 */
type GetAivenServiceAccountDetailsParams = ResolveIntersectionTypes<
  Omit<
    KlawApiRequestQueryParameters<"getAivenServiceAccountDetails">,
    "userName"
  > & {
    serviceName: KlawApiRequestQueryParameters<"getAivenServiceAccountDetails">["userName"];
  }
>;

type getAivenServiceAccountDetailsResponse =
  KlawApiResponse<"getAivenServiceAccountDetails">;
function getAivenServiceAccountDetails(
  params: GetAivenServiceAccountDetailsParams
): Promise<getAivenServiceAccountDetailsResponse> {
  const apiParams: KlawApiRequestQueryParameters<"getAivenServiceAccountDetails"> =
    {
      aclReqNo: params.aclReqNo,
      env: params.env,
      topicName: params.topicName,
      userName: params.serviceName,
    };

  return api.get<getAivenServiceAccountDetailsResponse>(
    API_PATHS.getAivenServiceAccountDetails,
    new URLSearchParams(apiParams)
  );
}

type GetConsumerOffsetsParams =
  KlawApiRequestQueryParameters<"getConsumerOffsets">;
type GetConsumerOffsetsResponse = KlawApiResponse<"getConsumerOffsets">;
function getConsumerOffsets(
  params: GetConsumerOffsetsParams
): Promise<GetConsumerOffsetsResponse> {
  return api.get<GetConsumerOffsetsResponse>(
    API_PATHS.getConsumerOffsets,
    new URLSearchParams(params)
  );
}

export {
  createAclRequest,
  getAclRequestsForApprover,
  getAclRequests,
  approveAclRequest,
  declineAclRequest,
  deleteAclRequest,
  getAivenServiceAccounts,
  getAivenServiceAccountDetails,
  requestAclDeletion,
  getConsumerOffsets,
};
