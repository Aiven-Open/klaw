import isString from "lodash/isString";
import { Environment } from "src/domain/environment/environment-types";
import { KlawApiResponse } from "types/utils";

function parseNumberOrUndefined(value: string | null): number | undefined {
  return isString(value) ? parseInt(value, 10) : undefined;
}

function transformEnvironmentApiResponse(
  apiResponse: KlawApiResponse<"environmentsGet">
): Environment[] {
  return apiResponse.map((environment) => {
    const rv: Environment = {
      name: environment.name,
      id: environment.id,
      defaultPartitions: parseNumberOrUndefined(environment.defaultPartitions),
      defaultReplicationFactor: parseNumberOrUndefined(
        environment.defaultReplicationFactor
      ),
      maxPartitions: parseNumberOrUndefined(environment.maxPartitions),
      maxReplicationFactor: parseNumberOrUndefined(
        environment.maxReplicationFactor
      ),
      topicNamePrefix: environment.topicprefix ?? undefined,
      topicNameSuffix: environment.topicsuffix ?? undefined,
      type: environment.type,
    };
    return rv;
  });
}

export { transformEnvironmentApiResponse };
