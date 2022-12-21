import pick from "lodash/pick";
import { Environment } from "src/domain/environment/environment-types";
import { KlawApiResponse } from "types/utils";

function transformEnvironmentApiResponse(
  apiResponse: KlawApiResponse<"environmentsGet">
): Environment[] {
  return apiResponse.map((environment) => pick(environment, ["name", "id"]));
}

export { transformEnvironmentApiResponse };
