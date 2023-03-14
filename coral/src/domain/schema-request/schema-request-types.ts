import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type CreatedSchemaRequests = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      | "req_no"
      | "topicname"
      | "environmentName"
      | "username"
      | "requesttimestring"
    >
  >
>;

type SchemaRequest = ResolveIntersectionTypes<KlawApiModel<"SchemaRequest">>;

type SchemaRequestApiResponse = ResolveIntersectionTypes<
  Paginated<SchemaRequest[]>
>;

export type { CreatedSchemaRequests, SchemaRequest, SchemaRequestApiResponse };
