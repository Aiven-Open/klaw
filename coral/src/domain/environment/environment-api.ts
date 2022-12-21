import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { Environment } from "src/domain/environment/environment-types";
import api from "src/services/api";
import { KlawApiResponse } from "types/utils";

const getEnvironments = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"environmentsGet">>("/getEnvs")
    .then(transformEnvironmentApiResponse);
};

export { getEnvironments };
