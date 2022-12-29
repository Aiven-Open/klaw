import { postAclRequest } from "src/domain/acl/acl-api";
import {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
} from "src/domain/acl/acl-types";

const createAclRequest = (
  aclParams:
    | CreateAclRequestTopicTypeProducer
    | CreateAclRequestTopicTypeConsumer
) => {
  return {
    mutationFn: () => postAclRequest(aclParams),
  };
};

export { createAclRequest };
