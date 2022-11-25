import api from "src/services/api";
import { TeamNamesGetResponse } from "src/domain/team/team-types";
import { transformTeamNamesGetResponse } from "src/domain/team/team-transformer";

const getTeams = () => {
  return api
    .get<TeamNamesGetResponse>("/getAllTeamsSUOnly")
    .then(transformTeamNamesGetResponse);
};

export { getTeams };
