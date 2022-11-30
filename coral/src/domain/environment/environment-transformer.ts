import pick from "lodash/pick";
import {
  Environment,
  EnvironmentsGetResponse,
} from "src/domain/environment/environment-types";

function transformEnvironmentApiResponse(
  apiResponse: EnvironmentsGetResponse
): Environment[] {
  return apiResponse.map((environment) => pick(environment, ["name", "id"]));
}

export { transformEnvironmentApiResponse };
