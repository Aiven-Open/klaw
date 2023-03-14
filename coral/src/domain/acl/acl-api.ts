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

type DeclineAclRequestPayload = RequestVerdictDecline<"ACL">;
const declineAclRequest = (payload: DeclineAclRequestPayload) => {
  return api.post<KlawApiResponse<"declineRequest">, DeclineAclRequestPayload>(
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
