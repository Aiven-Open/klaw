import {
  transformEnvironmentApiResponse,
  transformPaginatedEnvironmentApiResponse,
} from "src/domain/environment/environment-transformer";
import {
  Environment,
  EnvironmentPaginatedApiResponse,
} from "src/domain/environment/environment-types";
import api, { API_PATHS } from "src/services/api";
import { convertQueryValuesToString } from "src/services/api-helper";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

//  For each entity, there should be two GET endpoints for environments:
// - one gets *all* environments for that entity: used for browsing (list tables, Approval tables and My requests tables)
// - one gets environments *filtered according to the admins rules*: used for creating requests (forms)

// Exceptions to this rule:
// - ACL and Topic share the same environments
// - ACL requests cannot be promoted, so they have access to all environments in their creation form
// - @TODO: connector currently do not have a filtered endpoint, but it is a mistake to be corrected

const getAllEnvironmentsForTopicAndAcl = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<KlawApiResponse<"getEnvs">>(
    API_PATHS.getEnvs
  );

  return transformEnvironmentApiResponse(apiResponse);
};

const getPaginatedEnvironmentsForTopicAndAcl = async (
  params: KlawApiRequestQueryParameters<"getKafkaEnvsPaginated">
): Promise<EnvironmentPaginatedApiResponse> => {
  const queryParams = convertQueryValuesToString({
    pageNo: params.pageNo,
    ...(params.searchEnvParam && { searchEnvParam: params.searchEnvParam }),
  });

  const apiResponse = await api.get<KlawApiResponse<"getKafkaEnvsPaginated">>(
    API_PATHS.getKafkaEnvsPaginated,
    new URLSearchParams(queryParams)
  );

  return transformPaginatedEnvironmentApiResponse(apiResponse);
};

const getEnvironmentsForTopicRequest = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<KlawApiResponse<"getEnvsBaseCluster">>(
    API_PATHS.getEnvsBaseCluster
  );

  return transformEnvironmentApiResponse(apiResponse);
};

const getAllEnvironmentsForSchema = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<KlawApiResponse<"getSchemaRegEnvs">>(
    API_PATHS.getSchemaRegEnvs
  );

  return transformEnvironmentApiResponse(apiResponse);
};

const getPaginatedEnvironmentsForSchema = async (
  params: KlawApiRequestQueryParameters<"getSchemaRegEnvsPaginated">
): Promise<EnvironmentPaginatedApiResponse> => {
  const queryParams = convertQueryValuesToString({
    pageNo: params.pageNo,
    ...(params.searchEnvParam && { searchEnvParam: params.searchEnvParam }),
  });

  const apiResponse = await api.get<
    KlawApiResponse<"getSchemaRegEnvsPaginated">
  >(API_PATHS.getKafkaEnvsPaginated, new URLSearchParams(queryParams));

  return transformPaginatedEnvironmentApiResponse(apiResponse);
};

const getEnvironmentsForSchemaRequest = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<
    KlawApiResponse<"getEnvsForSchemaRequests">
  >(API_PATHS.getEnvsForSchemaRequests);

  return transformEnvironmentApiResponse(apiResponse);
};

const getAllEnvironmentsForConnector = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<KlawApiResponse<"getSyncConnectorsEnv">>(
    API_PATHS.getSyncConnectorsEnv
  );

  return transformEnvironmentApiResponse(apiResponse);
};

const getPaginatedEnvironmentsForConnector = async (
  params: KlawApiRequestQueryParameters<"getKafkaConnectEnvsPaginated">
): Promise<EnvironmentPaginatedApiResponse> => {
  const queryParams = convertQueryValuesToString({
    pageNo: params.pageNo,
    ...(params.searchEnvParam && { searchEnvParam: params.searchEnvParam }),
  });

  const apiResponse = await api.get<
    KlawApiResponse<"getKafkaConnectEnvsPaginated">
  >(API_PATHS.getKafkaEnvsPaginated, new URLSearchParams(queryParams));

  return transformPaginatedEnvironmentApiResponse(apiResponse);
};

export {
  getAllEnvironmentsForTopicAndAcl,
  getPaginatedEnvironmentsForTopicAndAcl,
  getEnvironmentsForTopicRequest,
  getAllEnvironmentsForSchema,
  getPaginatedEnvironmentsForSchema,
  getEnvironmentsForSchemaRequest,
  getAllEnvironmentsForConnector,
  getPaginatedEnvironmentsForConnector,
};
