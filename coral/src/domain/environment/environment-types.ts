import {
  KlawApiModel,
  KlawApiRequestQueryParameters,
  Paginated,
  ResolveIntersectionTypes,
} from "types/utils";

type Environment = KlawApiModel<"EnvModelResponse">;

type EnvironmentInfo = KlawApiModel<"EnvIdInfo">;
interface PaginatedEnvironmentsWithTotalEnvs extends Paginated<Environment[]> {
  totalEnvs: number;
}

type EnvironmentPaginatedApiResponse =
  ResolveIntersectionTypes<PaginatedEnvironmentsWithTotalEnvs>;

type GetKafkaEnvsPaginated = (
  params: KlawApiRequestQueryParameters<"getKafkaEnvsPaginated">
) => Promise<EnvironmentPaginatedApiResponse>;
type GetSchemaRegEnvsPaginated = (
  params: KlawApiRequestQueryParameters<"getSchemaRegEnvsPaginated">
) => Promise<EnvironmentPaginatedApiResponse>;
type GetKafkaConnectEnvsPaginated = (
  params: KlawApiRequestQueryParameters<"getKafkaConnectEnvsPaginated">
) => Promise<EnvironmentPaginatedApiResponse>;

const ALL_ENVIRONMENTS_VALUE = "ALL";

export type {
  Environment,
  EnvironmentInfo,
  EnvironmentPaginatedApiResponse,
  GetKafkaEnvsPaginated,
  GetSchemaRegEnvsPaginated,
  GetKafkaConnectEnvsPaginated,
};
export { ALL_ENVIRONMENTS_VALUE };
