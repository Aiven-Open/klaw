import omitBy from "lodash/omitBy";
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
  console.log("api call");
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
