import { KlawApiModel, ResolveIntersectionTypes } from "types/utils";

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
