import {
  ALL_TEAMS_VALUE,
  TeamName,
  Team,
  TEAM_NOT_INITIALIZED,
} from "src/domain/team/team-types";
import { getTeams, getTeamsOfUser } from "src/domain/team/team-api";

export type { TeamName, Team };
export { getTeams, ALL_TEAMS_VALUE, TEAM_NOT_INITIALIZED, getTeamsOfUser };
