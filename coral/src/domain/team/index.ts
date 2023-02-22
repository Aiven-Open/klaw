import {
  ALL_TEAMS_VALUE,
  Team,
  TEAM_NOT_INITIALIZED,
} from "src/domain/team/team-types";
import { getTeamNames } from "src/domain/team/team-api";

export type { Team };
export { getTeamNames, ALL_TEAMS_VALUE, TEAM_NOT_INITIALIZED };
