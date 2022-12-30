import { KlawApiRequest } from "types/utils";

// Several types are dependent on topictype when it is "Consumer":
// - aclPatternType can only be "LITERAL"
// - consumergroup is required
// - transactionalId becomes available
// So we define two types depending on the topictype

type CreateAclRequestTopicTypeProducer = Pick<
  KlawApiRequest<"createAclRequest">,
  | "remarks"
  | "aclPatternType"
  | "topicname"
  | "environment"
  | "teamname"
  | "aclIpPrincipleType"
  | "consumergroup"
  | "acl_ssl"
  | "acl_ip"
> & { topictype: "Producer" };

type CreateAclRequestTopicTypeConsumer = Pick<
  KlawApiRequest<"createAclRequest">,
  | "remarks"
  | "aclPatternType"
  | "topicname"
  | "environment"
  | "teamname"
  | "aclIpPrincipleType"
  | "acl_ssl"
  | "acl_ip"
  | "transactionalId"
> & {
  topictype: "Consumer";
  aclPatternType: "LITERAL";
  consumergroup: string;
};

export type {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
};
