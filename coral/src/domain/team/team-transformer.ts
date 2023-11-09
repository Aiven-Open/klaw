import { KlawApiResponse } from "types/utils";

function transformTeamNamesGetResponse(
  response: KlawApiResponse<"getAllTeamsSUOnly">
): string[] {
  return response.filter((name) => name !== "All teams");
}

export { transformTeamNamesGetResponse };
