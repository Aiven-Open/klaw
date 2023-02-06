import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
  GetCreatedAclRequestParameters,
} from "src/domain/acl/acl-types";
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

const getCreatedAclRequests = (params: GetCreatedAclRequestParameters) => {
  return api.get<KlawApiResponse<"getCreatedAclRequests">>(
    `/getCreatedAclRequests?${new URLSearchParams(params)}`
  );
};

export { createAclRequest, getCreatedAclRequests };
