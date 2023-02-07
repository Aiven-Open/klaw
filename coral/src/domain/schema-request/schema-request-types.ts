import { KlawApiModel, Prettify } from "types/utils";

type SchemaRequest = Prettify<
  Required<
    Pick<
      KlawApiModel<"SchemaRequest">,
      "environment" | "schemafull" | "topicname"
    >
  > &
    Pick<KlawApiModel<"SchemaRequest">, "remarks">
>;

type SchemaRequestPayload = Prettify<
  SchemaRequest & {
    // schemaversion and appname
    // should be hard coded at the moment
    schemaversion: "1.0";
    appname: "App";
  }
>;

export type { SchemaRequest, SchemaRequestPayload };
