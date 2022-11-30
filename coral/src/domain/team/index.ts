import {
  ALL_TEAMS_VALUE,
  Team,
  TEAM_NOT_INITIALIZED,
} from "src/domain/team/team-types";
import { getTeams } from "src/domain/team/team-api";

export type { Team };
export { getTeams, ALL_TEAMS_VALUE, TEAM_NOT_INITIALIZED };
