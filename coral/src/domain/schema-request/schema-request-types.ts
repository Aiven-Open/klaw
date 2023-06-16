import {
  KlawApiModel,
  KlawApiRequest,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";

type CreatedSchemaRequests = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequestsResponseModel">,
      | "req_no"
      | "topicname"
      | "environmentName"
      | "requestor"
      | "requesttimestring"
    >
  >
>;

type SchemaRequest = ResolveIntersectionTypes<
  KlawApiModel<"SchemaRequestsResponseModel">
>;

type SchemaRequestApiResponse = ResolveIntersectionTypes<
  Paginated<SchemaRequest[]>
>;

type PromoteSchemaPayload = KlawApiRequest<"promoteSchema">;

export type {
  CreatedSchemaRequests,
  SchemaRequest,
  SchemaRequestApiResponse,
  PromoteSchemaPayload,
};
