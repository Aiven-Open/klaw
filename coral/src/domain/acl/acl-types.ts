import {
  KlawApiModel,
  KlawApiRequest,
  KlawApiRequestQueryParameters,
  Paginated,
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
  KlawApiRequest<"createAcl">,
  | "remarks"
  | "aclPatternType"
  | "aclType"
  | "topicname"
  | "environment"
  | "teamId"
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

type GetCreatedAclRequestForApproverParameters = ResolveIntersectionTypes<
  Omit<KlawApiRequestQueryParameters<"getAclRequestsForApprover">, "aclType">
> & {
  aclType?: "ALL" | "PRODUCER" | "CONSUMER";
};

type GetCreatedAclRequestParameters = ResolveIntersectionTypes<
  Omit<KlawApiRequestQueryParameters<"getAclRequests">, "aclType">
> & {
  aclType?: "ALL" | "PRODUCER" | "CONSUMER";
};

type AclRequest = KlawApiModel<"AclRequestsResponseModel">;

type AclRequestsForApprover = ResolveIntersectionTypes<Paginated<AclRequest[]>>;

type AclType = KlawApiModel<"AclRequestsResponseModel">["aclType"];

type ConsumerOffsets = KlawApiModel<"OffsetDetails">;

type ServiceAccountDetails = KlawApiModel<"ServiceAccountDetails">;

export type {
  AclRequest,
  AclRequestsForApprover,
  AclType,
  ConsumerOffsets,
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
  GetCreatedAclRequestForApproverParameters,
  GetCreatedAclRequestParameters,
  ServiceAccountDetails,
};
