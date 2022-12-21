import { Team } from "src/domain/team/team-types";
import { KlawApiResponse } from "types/utils";

function transformTeamNamesGetResponse(
  response: KlawApiResponse<"teamNamesGet">
): Team[] {
  return response.filter((name) => name !== "All teams");
}

export { transformTeamNamesGetResponse };
