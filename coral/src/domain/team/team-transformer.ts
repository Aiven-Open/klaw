import { Team, TeamNamesGetResponse } from "src/domain/team/team-types";

function transformTeamNamesGetResponse(response: TeamNamesGetResponse): Team[] {
  return response.filter((name) => name !== "All teams");
}

export { transformTeamNamesGetResponse };
