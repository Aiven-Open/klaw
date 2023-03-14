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
  KlawApiRequest<"createAcl">,
  | "remarks"
  | "aclPatternType"
  | "aclType"
  | "topicname"
  | "environment"
  | "teamname"
  | "aclIpPrincipleType"
  | "acl_ssl"
  | "acl_ip"
>;

type CreateAclRequestTopicTypeProducer = ResolveIntersectionTypes<
  BaseCreateAclRequest & {
    aclType: "PRODUCER";
  }
>;

type CreateAclRequestTopicTypeConsumer = ResolveIntersectionTypes<
  BaseCreateAclRequest & {
    transactionalId?: string;
    aclType: "CONSUMER";
    aclPatternType: "LITERAL";
    consumergroup: string;
  }
>;

type GetCreatedAclRequestParameters = ResolveIntersectionTypes<
  Omit<KlawApiRequestQueryParameters<"getAclRequestsForApprover">, "aclType">
> & {
  aclType?: "ALL" | "PRODUCER" | "CONSUMER";
};

type AclRequest = KlawApiModel<"AclRequestsModel">;

type AclRequestsForApprover = ResolveIntersectionTypes<Paginated<AclRequest[]>>;

type AclType = KlawApiModel<"AclRequestsModel">["aclType"];

export type {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
  GetCreatedAclRequestParameters,
  AclRequest,
  AclRequestsForApprover,
  AclType,
};
