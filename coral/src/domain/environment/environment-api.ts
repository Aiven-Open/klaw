import { transformEnvironmentApiResponse } from "src/domain/environment/environment-transformer";
import { Environment } from "src/domain/environment/environment-types";

const getEnvironments = async (): Promise<Environment[]> => {
  return fetch(`/getEnvs`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`msw error: ${response.statusText}`);
      }
      const result = await response.json();
      return transformEnvironmentApiResponse(result);
    })
    .catch((error) => {
      throw new Error(error);
    });
};

export { getEnvironments };
