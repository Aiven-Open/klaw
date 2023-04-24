import isString from "lodash/isString";
import { Environment } from "src/domain/environment/environment-types";
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
          topicPrefix: environment.params.topicPrefix,
          topicSuffix: environment.params.topicSuffix,
          topicRegex: environment.params.topicRegex,
      },}),
      type: environment?.type,
    };
    return rv;
  });
}

export { transformEnvironmentApiResponse };
