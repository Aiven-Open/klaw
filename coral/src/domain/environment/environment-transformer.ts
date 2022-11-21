import {
  Environment,
  EnvironmentDTO,
} from "src/domain/environment/environment-types";

function transformEnvironmentApiResponse(
  apiResponse: EnvironmentDTO[]
): Environment[] {
  return [...new Set(apiResponse.map((topicEnv) => topicEnv.name))].sort();
}

export { transformEnvironmentApiResponse };
