import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import {
  Environment,
  EnvironmentsGetResponse,
} from "src/domain/environment/environment-types";
import api from "src/services/api";

const getEnvironments = async (): Promise<Environment[]> => {
  return api
    .get<EnvironmentsGetResponse>("/getEnvs")
    .then(transformEnvironmentApiResponse);
};

export { getEnvironments };
