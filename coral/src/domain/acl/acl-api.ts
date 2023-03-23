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
import api from "src/services/api";
import { KlawApiRequest, KlawApiResponse } from "types/utils";

const createAclRequest = (
  aclParams:
    | CreateAclRequestTopicTypeProducer
    | CreateAclRequestTopicTypeConsumer
): Promise<KlawApiResponse<"createAcl">> => {
  return api.post<KlawApiResponse<"createAcl">, KlawApiRequest<"createAcl">>(
    "/createAcl",
    aclParams
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
      const omitOperationType = property === "operationType" && value === "ALL";

      return (
        omitEnv ||
        omitAclType ||
        omitTopic ||
        omitOperationType ||
        omitIsMyRequest
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
      `/getAclRequestsForApprover?${new URLSearchParams(filteredParams)}`
    )
    .then(transformAclRequestApiResponse);
};

const getAclRequests = (params: GetCreatedAclRequestParameters) => {
  const filteredParams = filterGetAclRequestParams(params);

  return api
    .get<KlawApiResponse<"getAclRequests">>(
      `/getAclRequests?${new URLSearchParams(filteredParams)}`
    )
    .then(transformAclRequestApiResponse);
};

type ApproveAclRequestPayload = RequestVerdictApproval<"ACL">;
type ApproveRequestParams = {
  reqIds: ApproveAclRequestPayload["reqIds"];
};
const approveAclRequest = ({ reqIds }: ApproveRequestParams) => {
  return api.post<KlawApiResponse<"approveRequest">, ApproveAclRequestPayload>(
    `/request/approve`,
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
    `/request/decline`,
    { requestEntityType: "ACL", reqIds, reason }
  );
};

type DeleteAclRequestPayload = RequestVerdictDelete<"ACL">;
type DeleteRequestParams = {
  reqIds: DeleteAclRequestPayload["reqIds"];
};
const deleteAclRequest = ({ reqIds }: DeleteRequestParams) => {
  return api.post<KlawApiResponse<"deleteRequest">, DeleteAclRequestPayload>(
    `/request/delete`,
    { requestEntityType: "ACL", reqIds }
  );
};

export {
  createAclRequest,
  getAclRequestsForApprover,
  getAclRequests,
  approveAclRequest,
  declineAclRequest,
  deleteAclRequest,
};
