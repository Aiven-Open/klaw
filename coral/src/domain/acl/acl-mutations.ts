import { createAclRequest } from "src/domain/acl/acl-api";
import {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
} from "src/domain/acl/acl-types";

const createAclRequestMutation = (
  aclParams:
    | CreateAclRequestTopicTypeProducer
    | CreateAclRequestTopicTypeConsumer
) => {
  return {
    mutationFn: () => createAclRequest(aclParams),
  };
};

export { createAclRequestMutation };
