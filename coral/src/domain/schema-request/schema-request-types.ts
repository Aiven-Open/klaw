import { KlawApiModel, ResolveIntersectionTypes } from "types/utils";

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

type SchemaRequestPayload = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Pick<KlawApiModel<"SchemaRequest">, "remarks" | "schemaversion" | "appname">
>;

export type { SchemaRequestPayload, CreatedSchemaRequests };
