import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiModel,
  ResolveIntersectionTypes,
  Paginated,
} from "types/utils";

// Several types are dependent on topictype when it is "Consumer":
// - aclPatternType can only be "LITERAL"
// - consumergroup is required
// - transactionalId becomes available
// So we define three types:
// - BaseCreateAclRequest has all the properties for a create ACL request
// - CreateAclRequestTopicTypeProducer makes the relevant property mandatory
// - CreateAclRequestTopicTypeConsumer adds transactionalId and make relevant properties mandatory

type BaseCreateAclRequest = Pick<
  KlawApiRequest<"createAclRequest">,
  | "remarks"
  | "aclPatternType"
  | "topictype"
  | "topicname"
  | "environment"
  | "teamname"
  | "aclIpPrincipleType"
  | "acl_ssl"
  | "acl_ip"
>;

type CreateAclRequestTopicTypeProducer = ResolveIntersectionTypes<
  BaseCreateAclRequest & {
    topictype: "Producer";
  }
>;

type CreateAclRequestTopicTypeConsumer = ResolveIntersectionTypes<
  BaseCreateAclRequest & {
    transactionalId?: string;
    topictype: "Consumer";
    aclPatternType: "LITERAL";
    consumergroup: string;
  }
>;

type GetCreatedAclRequestParameters = ResolveIntersectionTypes<
  Omit<KlawApiRequestQueryParameters<"getAclRequestsForApprover">, "aclType">
> & {
  aclType?: "ALL" | "PRODUCER" | "CONSUMER";
};

type AclRequest = KlawApiModel<"aclRequest">;

type AclRequestsForApprover = ResolveIntersectionTypes<Paginated<AclRequest[]>>;

export type {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
  GetCreatedAclRequestParameters,
  AclRequest,
  AclRequestsForApprover,
};
