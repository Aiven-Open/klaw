import omitBy from "lodash/omitBy";
import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
  GetCreatedAclRequestParameters,
} from "src/domain/acl/acl-types";
import {
  RequestVerdictApproval,
  RequestVerdictDecline,
} from "src/domain/requests";
import api from "src/services/api";
import { KlawApiRequest, KlawApiResponse } from "types/utils";

const createAclRequest = (
  aclParams:
    | CreateAclRequestTopicTypeProducer
    | CreateAclRequestTopicTypeConsumer
): Promise<KlawApiResponse<"createAclRequest">> => {
  return api.post<
    KlawApiResponse<"createAclRequest">,
    KlawApiRequest<"createAclRequest">
  >("/createAcl", aclParams);
};

const getAclRequestsForApprover = (params: GetCreatedAclRequestParameters) => {
  const filteredParams = omitBy(params, (value, property) => {
    const omitEnv = property === "env" && value === "ALL";
    const omitAclType = property === "aclType" && value === "ALL";
    const omitTopic = property === "topic" && value === "";

    return omitEnv || omitAclType || omitTopic;
  });

  return api
    .get<KlawApiResponse<"getAclRequestsForApprover">>(
      `/getAclRequestsForApprover?${new URLSearchParams(filteredParams)}`
    )
    .then(transformAclRequestApiResponse);
};

type ApproveAclRequestPayload = RequestVerdictApproval<"ACL">;
const approveAclRequest = (payload: ApproveAclRequestPayload) => {
  return api.post<KlawApiResponse<"approveRequest">, ApproveAclRequestPayload>(
    `/request/approve`,
    payload
  );
};

type RejectAclRequestPayload = RequestVerdictDecline<"ACL">;
const declineAclRequest = (payload: RejectAclRequestPayload) => {
  return api.post<KlawApiResponse<"declineRequest">, RejectAclRequestPayload>(
    `/request/decline`,
    payload
  );
};

export {
  createAclRequest,
  getAclRequestsForApprover,
  approveAclRequest,
  declineAclRequest,
};
