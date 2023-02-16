import {
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  KlawApiModel,
  ResolveIntersectionTypes,
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

type GetCreatedAclRequestParameters =
  KlawApiRequestQueryParameters<"getAclRequestsForApprover">;

type AclRequest = KlawApiModel<"aclRequest">;

type Paginated<T> = {
  totalPages: number;
  currentPage: number;
  entries: T;
};

type AclRequestsForApprover = Paginated<AclRequest[]>;

export type {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
  GetCreatedAclRequestParameters,
  AclRequest,
  AclRequestsForApprover,
};
