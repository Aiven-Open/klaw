import {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
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

export { createAclRequest };
