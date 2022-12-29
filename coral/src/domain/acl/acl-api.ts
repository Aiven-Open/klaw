import {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
} from "src/domain/acl/acl-types";
import api from "src/services/api";
import { KlawApiResponse } from "types/utils";

const postAclRequest = (
  aclParams:
    | CreateAclRequestTopicTypeProducer
    | CreateAclRequestTopicTypeConsumer
): Promise<KlawApiResponse<"createAclRequest">> => {
  return api.post("/createAcl", aclParams);
};

export { postAclRequest };
