import {
  Environment,
  EnvironmentPaginatedApiResponse,
} from "src/domain/environment/environment-types";
import { KlawApiModel } from "types/utils";

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
    const rv: Environment = environment;
    return rv;
  });

  return {
    totalPages: Number(apiResponse[0].totalNoPages),
    currentPage: Number(apiResponse[0].currentPage),
    totalEnvs: Number(apiResponse[0].totalRecs),
    entries: envData,
  };
}

export { transformPaginatedEnvironmentApiResponse };
