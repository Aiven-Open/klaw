import { TeamName } from "src/domain/team/team-types";
import { KlawApiResponse } from "types/utils";

function transformTeamNamesGetResponse(
  response: KlawApiResponse<"getAllTeamsSUOnly">
): TeamName[] {
  return response.filter((name) => name !== "All teams");
}

export { transformTeamNamesGetResponse };
