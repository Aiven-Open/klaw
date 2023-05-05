import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { Environment } from "src/domain/environment/environment-types";
import api, { API_PATHS } from "src/services/api";
import { KlawApiResponse } from "types/utils";

const getAllEnvironments = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvs">>(API_PATHS.getEnvs)
    .then(transformEnvironmentApiResponse);
};

const getEnvironmentsForTopicRequest = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvsBaseCluster">>(API_PATHS.getEnvsBaseCluster)
    .then(transformEnvironmentApiResponse);
};

const getEnvironmentsForTeam = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvsBaseClusterFilteredForTeam">>(
      API_PATHS.getEnvsBaseClusterFilteredForTeam
    )
    .then(transformEnvironmentApiResponse);
};

const getSchemaRegistryEnvironments = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvsForSchemaRequests">>(
      API_PATHS.getEnvsForSchemaRequests
    )
    .then(transformEnvironmentApiResponse);
};

const getSyncConnectorsEnvironments = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getSyncConnectorsEnv">>(
      API_PATHS.getSyncConnectorsEnv
    )
    .then(transformEnvironmentApiResponse);
};

const getClusterInfo = async ({
  envSelected,
  envType,
}: {
  envSelected: string;
  envType: Environment["type"];
}): Promise<KlawApiResponse<"getClusterInfoFromEnv">> => {
  const params = new URLSearchParams({ envSelected, envType });
  return api.get<KlawApiResponse<"getClusterInfoFromEnv">>(
    API_PATHS.getClusterInfoFromEnv,
    params
  );
};

export {
  getAllEnvironments,
  getEnvironmentsForTopicRequest,
  getClusterInfo,
  getEnvironmentsForTeam,
  getSchemaRegistryEnvironments,
  getSyncConnectorsEnvironments,
};
