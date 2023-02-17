import transformAclRequestApiResponse from "src/domain/acl/acl-transformer";
import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
  GetCreatedAclRequestParameters,
} from "src/domain/acl/acl-types";
import api from "src/services/api";
import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiResponse,
} from "types/utils";

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
  if (params.env === "ALL") {
    delete params.env;
  }
  if (params.aclType === "ALL") {
    delete params.aclType;
  }
  if (params.topic === "") {
    delete params.topic;
  }
  return api
    .get<KlawApiResponse<"getAclRequestsForApprover">>(
      `/getAclRequestsForApprover?${new URLSearchParams(params)}`
    )
    .then(transformAclRequestApiResponse);
};

const approveAclRequest = (
  params: KlawApiRequestQueryParameters<"approveAclRequests">
): Promise<KlawApiResponse<"approveAclRequests">> => {
  return api.post<KlawApiResponse<"approveAclRequests">, never>(
    `/execAclRequest?${new URLSearchParams(params)}`
  );
};

const declineAclRequest = (
  params: KlawApiRequestQueryParameters<"declineAclRequests">
): Promise<KlawApiResponse<"declineAclRequests">> => {
  return api.post<KlawApiResponse<"declineAclRequests">, never>(
    `/execAclRequestDecline?${new URLSearchParams(params)}`
  );
};

export {
  createAclRequest,
  getAclRequestsForApprover,
  approveAclRequest,
  declineAclRequest,
};
