import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import {
  ClusterInfo,
  Environment,
} from "src/domain/environment/environment-types";
import api from "src/services/api";
import { KlawApiResponse } from "types/utils";

const getEnvironments = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvs">>("/getEnvs")
    .then(transformEnvironmentApiResponse);
};

const getEnvironmentsForTeam = (): Promise<Environment[]> => {
  const url = "/";
  return api
    .get<KlawApiResponse<"getEnvsBaseClusterFilteredForTeam">>(url)
    .then(transformEnvironmentApiResponse);
};

const getSchemaRegistryEnvironments = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getSchemaRegEnvs">>("/getSchemaRegEnvs")
    .then(transformEnvironmentApiResponse);
};

const getClusterInfo = async ({
  envSelected,
  envType,
}: {
  envSelected: string;
  envType: Environment["type"];
}): Promise<ClusterInfo> => {
  const params = new URLSearchParams({ envSelected, envType });
  return api.get<KlawApiResponse<"getClusterInfoFromEnv">>(
    `/getClusterInfoFromEnv?${params}`
  );
};

export {
  getEnvironments,
  getClusterInfo,
  getEnvironmentsForTeam,
  getSchemaRegistryEnvironments,
};
