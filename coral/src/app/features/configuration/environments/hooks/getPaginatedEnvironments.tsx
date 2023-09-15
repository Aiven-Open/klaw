import { useQuery } from "@tanstack/react-query";
import {
  getPaginatedEnvironmentsForConnector,
  getPaginatedEnvironmentsForSchema,
  getPaginatedEnvironmentsForTopicAndAcl,
} from "src/domain/environment";
import {
  GetKafkaConnectEnvsPaginated,
  GetKafkaEnvsPaginated,
  GetSchemaRegEnvsPaginated,
} from "src/domain/environment/environment-types";

interface EnvEndpointsTypesMap {
  kafka: GetKafkaEnvsPaginated;
  kafkaconnect: GetSchemaRegEnvsPaginated;
  schemaregistry: GetKafkaConnectEnvsPaginated;
}

type QueryKeysMap = {
  kafka: "getPaginatedEnvironmentsForTopicAndAcl";
  kafkaconnect: "getPaginatedEnvironmentsForConnector";
  schemaregistry: "getPaginatedEnvironmentsForSchema";
};

interface GetEnvironmentsParams {
  type: keyof EnvEndpointsTypesMap;
  currentPage: number;
  search?: string;
}

const envEndpointsKeys: QueryKeysMap = {
  kafka: "getPaginatedEnvironmentsForTopicAndAcl",
  kafkaconnect: "getPaginatedEnvironmentsForConnector",
  schemaregistry: "getPaginatedEnvironmentsForSchema",
};

const envEndpoints = {
  kafka: getPaginatedEnvironmentsForTopicAndAcl,
  kafkaconnect: getPaginatedEnvironmentsForConnector,
  schemaregistry: getPaginatedEnvironmentsForSchema,
} satisfies EnvEndpointsTypesMap;

const getPaginatedEnvironments = ({
  type,
  currentPage,
  search = "",
}: GetEnvironmentsParams) => {
  const { data, isLoading, isError, error } = useQuery(
    [envEndpointsKeys[type], currentPage, search],
    {
      queryFn: () =>
        envEndpoints[type]({
          pageNo: String(currentPage),
          searchEnvParam: search.length === 0 ? undefined : search,
        }),
    }
  );
  return {
    environments: data,
    isLoading,
    isError,
    error,
  };
};

export default getPaginatedEnvironments;
