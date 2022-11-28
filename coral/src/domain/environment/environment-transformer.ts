import {
  Environment,
  EnvironmentsGetResponse,
} from "src/domain/environment/environment-types";

function transformEnvironmentApiResponse(
  apiResponse: EnvironmentsGetResponse
): Environment[] {
  return [...new Set(apiResponse.map((topicEnv) => topicEnv.name))].sort();
}

export { transformEnvironmentApiResponse };
