import { transformPaginatedEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import {
  Environment,
  EnvironmentPaginatedApiResponse,
} from "src/domain/environment/environment-types";
import api, { API_PATHS } from "src/services/api";
import { convertQueryValuesToString } from "src/services/api-helper";
import { KlawApiRequestQueryParameters, KlawApiResponse } from "types/utils";

const getAllEnvironmentsForTopicAndAcl = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<KlawApiResponse<"getEnvs">>(
    API_PATHS.getEnvs
  );

  return apiResponse;
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

  return apiResponse;
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
  >(API_PATHS.getSchemaRegEnvsPaginated, new URLSearchParams(queryParams));

  return transformPaginatedEnvironmentApiResponse(apiResponse);
};

const getAllEnvironmentsForConnector = async (): Promise<Environment[]> => {
  const apiResponse = await api.get<KlawApiResponse<"getSyncConnectorsEnv">>(
    API_PATHS.getSyncConnectorsEnv
  );

  return apiResponse;
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
  >(API_PATHS.getKafkaConnectEnvsPaginated, new URLSearchParams(queryParams));

  return transformPaginatedEnvironmentApiResponse(apiResponse);
};

const getUpdateEnvStatus = async (
  params: KlawApiRequestQueryParameters<"getUpdateEnvStatus">
): Promise<KlawApiResponse<"getUpdateEnvStatus">> => {
  const apiResponse = await api.get<KlawApiResponse<"getUpdateEnvStatus">>(
    API_PATHS.getUpdateEnvStatus,
    new URLSearchParams(params)
  );

  return apiResponse;
};

export {
  getAllEnvironmentsForTopicAndAcl,
  getPaginatedEnvironmentsForTopicAndAcl,
  getEnvironmentsForTopicRequest,
  getPaginatedEnvironmentsForSchema,
  getAllEnvironmentsForConnector,
  getPaginatedEnvironmentsForConnector,
  getUpdateEnvStatus,
};
