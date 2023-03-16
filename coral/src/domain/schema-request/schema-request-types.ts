import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type CreatedSchemaRequests = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequestsResponseModel">,
      | "req_no"
      | "topicname"
      | "environmentName"
      | "username"
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

export type { CreatedSchemaRequests, SchemaRequest, SchemaRequestApiResponse };
