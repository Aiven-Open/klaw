import { KlawApiModel, ResolveIntersectionTypes } from "types/utils";

<<<<<<< Updated upstream
type SchemaRequest = ResolveIntersectionTypes<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Pick<KlawApiModel<"SchemaRequest">, "remarks">
>;

type SchemaRequestPayload = ResolveIntersectionTypes<
  SchemaRequest & {
    // schemaversion and appname
    // should be hard coded at the moment
    schemaversion: "1.0";
    appname: "App";
  }
>;

export type { SchemaRequest, SchemaRequestPayload };
=======
type CreatedSchemaRequests = Required<
  Pick<
    KlawApiModel<"SchemaRequest">,
    | "req_no"
    | "topicname"
    | "environmentName"
    | "username"
    | "requesttimestring"
  >
>;

type SchemaRequestPayload = Required<
  Pick<
    KlawApiModel<"SchemaRequest">,
    "environment" | "schemafull" | "topicname"
  >
> &
  Pick<KlawApiModel<"SchemaRequest">, "remarks" | "schemaversion" | "appname">;

export type { SchemaRequestPayload, CreatedSchemaRequests };
>>>>>>> Stashed changes
