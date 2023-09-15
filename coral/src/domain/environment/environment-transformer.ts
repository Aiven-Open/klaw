import isString from "lodash/isString";
import {
  Environment,
  EnvironmentPaginatedApiResponse,
} from "src/domain/environment/environment-types";
import { KlawApiModel } from "types/utils";

function parseNumberOrUndefined(value: string | undefined): number | undefined {
  return isString(value) ? parseInt(value, 10) : undefined;
}

function transformEnvironmentApiResponse(
  apiResponse: KlawApiModel<"EnvModelResponse">[]
): Environment[] {
  return apiResponse.map((environment) => {
    const rv: Environment = {
      name: environment.name,
      id: environment.id,
      ...(environment.params && {
        params: {
          defaultPartitions: parseNumberOrUndefined(
            environment.params.defaultPartitions
          ),
          defaultRepFactor: parseNumberOrUndefined(
            environment.params.defaultRepFactor
          ),
          maxPartitions: parseNumberOrUndefined(
            environment.params.maxPartitions
          ),
          maxRepFactor: parseNumberOrUndefined(environment.params.maxRepFactor),
          applyRegex: environment.params.applyRegex,
          topicPrefix: environment.params.topicPrefix?.filter(
            (prefix) => prefix.length > 0
          ),
          topicSuffix: environment.params.topicSuffix?.filter(
            (suffix) => suffix.length > 0
          ),
          topicRegex: environment.params.topicRegex?.filter(
            (regex) => regex.length > 0
          ),
        },
      }),
      type: environment?.type,
      clusterName: environment?.clusterName,
      tenantName: environment?.tenantName,
      envStatus: environment?.envStatus,
      associatedEnv: environment?.associatedEnv,
    };
    return rv;
  });
}

function transformPaginatedEnvironmentApiResponse(
  apiResponse: KlawApiModel<"EnvModelResponse">[]
): EnvironmentPaginatedApiResponse {
  if (apiResponse.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      totalEnvs: 0,
      entries: [],
    };
  }

  const envData = apiResponse.map((environment) => {
    const rv: Environment = {
      name: environment.name,
      id: environment.id,
      ...(environment.params && {
        params: {
          defaultPartitions: parseNumberOrUndefined(
            environment.params.defaultPartitions
          ),
          defaultRepFactor: parseNumberOrUndefined(
            environment.params.defaultRepFactor
          ),
          maxPartitions: parseNumberOrUndefined(
            environment.params.maxPartitions
          ),
          maxRepFactor: parseNumberOrUndefined(environment.params.maxRepFactor),
          applyRegex: environment.params.applyRegex,
          topicPrefix: environment.params.topicPrefix?.filter(
            (prefix) => prefix.length > 0
          ),
          topicSuffix: environment.params.topicSuffix?.filter(
            (suffix) => suffix.length > 0
          ),
          topicRegex: environment.params.topicRegex?.filter(
            (regex) => regex.length > 0
          ),
        },
      }),
      type: environment?.type,
      clusterName: environment?.clusterName,
      tenantName: environment?.tenantName,
      envStatus: environment?.envStatus,
      associatedEnv: environment?.associatedEnv,
    };
    return rv;
  });

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    currentPage: Number(apiResponse[0].currentPage),
    totalEnvs: Number(apiResponse[0].totalRecs),
    entries: envData.flat(),
  };
}

export {
  transformEnvironmentApiResponse,
  transformPaginatedEnvironmentApiResponse,
};
