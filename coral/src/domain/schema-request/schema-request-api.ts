import { KlawApiRequest, KlawApiResponse } from "types/utils";
import api from "src/services/api";
import { SchemaRequestPayload } from "src/domain/schema-request/schema-request-types";

const createSchemaRequest = (
  params: SchemaRequestPayload
): Promise<KlawApiResponse<"schemaUpload">> => {
  const payload = {
    ...params,
    schemaversion: "1.0",
    appname: "App",
  };

  return api.post<
    KlawApiResponse<"schemaUpload">,
    KlawApiRequest<"schemaUpload">
  >(`/uploadSchema`, payload);
};

export { createSchemaRequest };
