import { KlawApiModel } from "types/utils";

type SchemaRequest = Required<
  Pick<
    KlawApiModel<"SchemaRequest">,
    "environment" | "schemafull" | "topicname"
  >
> &
  Pick<KlawApiModel<"SchemaRequest">, "remarks">;

type SchemaRequestPayload = SchemaRequest & {
  // schemaversion and appname
  // should be hard coded at the moment
  schemaversion: "1.0";
  appname: "App";
};

export type { SchemaRequest, SchemaRequestPayload };
