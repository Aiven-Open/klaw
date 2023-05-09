import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { Environment } from "src/domain/environment/environment-types";
import api, { API_PATHS } from "src/services/api";
import { KlawApiResponse } from "types/utils";

// For each entity, there should be two GET endpoints for environments:
// - one gets *all* environments for that entity: used for browsing (list tables, Approval tables and My requests tables)
// - one gets environments *filtered according to the admin's rules*: used for creating requests (forms)

// Exceptions to this rule:
// - ACL and Topic share the same environments
// - ACL requests cannot be promoted, so they have access to all environments in their creation form
// - @TODO: connector currently do not have a filtered endpoint, but it is a mistake to be corrected

const getAllEnvironmentsForTopicAndAcl = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvs">>(API_PATHS.getEnvs)
    .then(transformEnvironmentApiResponse);
};

const getEnvironmentsForTopicAndAclRequest = async (): Promise<
  Environment[]
> => {
  return api
    .get<KlawApiResponse<"getEnvsBaseCluster">>(API_PATHS.getEnvsBaseCluster)
    .then(transformEnvironmentApiResponse);
};

const getTopicAndAclEnvironmentsForTeam = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvsBaseClusterFilteredForTeam">>(
      API_PATHS.getEnvsBaseClusterFilteredForTeam
    )
    .then(transformEnvironmentApiResponse);
};

const getAllEnvironmentsForSchema = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getSchemaRegEnvs">>(API_PATHS.getSchemaRegEnvs)
    .then(transformEnvironmentApiResponse);
};

const getEnvironmentsForSchemaRequest = (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvsForSchemaRequests">>(
      API_PATHS.getEnvsForSchemaRequests
    )
    .then(transformEnvironmentApiResponse);
};

const getAllEnvironmentsForConnector = (): Promise<Environment[]> => {
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
  getAllEnvironmentsForTopicAndAcl,
  getEnvironmentsForTopicAndAclRequest,
  getClusterInfo,
  getTopicAndAclEnvironmentsForTeam,
  getAllEnvironmentsForSchema,
  getEnvironmentsForSchemaRequest,
  getAllEnvironmentsForConnector,
};
