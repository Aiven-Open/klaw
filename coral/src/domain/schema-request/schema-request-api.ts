import { KlawApiRequest, KlawApiResponse } from "types/utils";
import api from "src/services/api";

const getSchemaRegistryEnvironments = (): Promise<
  KlawApiResponse<"schemaRegEnvsGet">
> => {
  return api.get<KlawApiResponse<"schemaRegEnvsGet">>("/getSchemaRegEnvs");
};

const createSchemaRequest = (
  params: KlawApiRequest<"schemaUpload">
): Promise<KlawApiResponse<"schemaUpload">> => {
  const schemaUploadParams = {
    ...params,
    // schemaversion and appname
    // should be hard coded at the moment
    schemaversion: "1.0",
    appname: "App",
  };
  return api.post<
    KlawApiResponse<"schemaUpload">,
    KlawApiRequest<"schemaUpload">
  >("/uploadSchema", schemaUploadParams);
};

export { createSchemaRequest, getSchemaRegistryEnvironments };
