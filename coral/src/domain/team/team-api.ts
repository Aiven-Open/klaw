import api from "src/services/api";
import { transformTeamNamesGetResponse } from "src/domain/team/team-transformer";
import { KlawApiResponse } from "types/utils";

const getTeamNames = () => {
  return api
    .get<KlawApiResponse<"getAllTeamsSUOnly">>("/getAllTeamsSUOnly")
    .then(transformTeamNamesGetResponse);
};

const getTeams = () => {
  return api.get<KlawApiResponse<"getAllTeamsSU">>("/getAllTeamsSU");
};

export { getTeamNames, getTeams };
