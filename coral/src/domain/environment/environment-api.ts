import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { Environment } from "src/domain/environment/environment-types";
import api, { API_PATHS } from "src/services/api";
import { KlawApiResponse } from "types/utils";

//  For each entity, there should be two GET endpoints for environments:
// - one gets *all* environments for that entity: used for browsing (list tables, Approval tables and My requests tables)
// - one gets environments *filtered according to the admins rules*: used for creating requests (forms)

// Exceptions to this rule:
// - ACL and Topic share the same environments
// - ACL requests cannot be promoted, so they have access to all environments in their creation form
// - @TODO: connector currently do not have a filtered endpoint, but it is a mistake to be corrected

const getAllEnvironmentsForTopicAndAcl = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvs">>(API_PATHS.getEnvs)
    .then(transformEnvironmentApiResponse);
};

const getEnvironmentsForTopicRequest = async (): Promise<Environment[]> => {
  return api
    .get<KlawApiResponse<"getEnvsBaseCluster">>(API_PATHS.getEnvsBaseCluster)
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

export {
  getAllEnvironmentsForTopicAndAcl,
  getEnvironmentsForTopicRequest,
  getAllEnvironmentsForSchema,
  getEnvironmentsForSchemaRequest,
  getAllEnvironmentsForConnector,
};
