import { components } from "types/api.d";
// "teamName" is a optional parameter for the getTopics api,
// but we still need an identifier in frontend
// to be able to set "All teams" as a visible option
// for the user, UUID makes sure we don't overlap with an
// actual team name in the future
const ALL_TEAMS_VALUE = "f5ed03b4-c0da-4b18-a534-c7e9a13d1342";

type Team = string | typeof ALL_TEAMS_VALUE;

type TeamNamesGetResponse = components["schemas"]["TeamNamesGetResponse"];

export type { Team, TeamNamesGetResponse };
export { ALL_TEAMS_VALUE };