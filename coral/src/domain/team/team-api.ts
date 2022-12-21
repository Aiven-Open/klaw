import api from "src/services/api";
import { transformTeamNamesGetResponse } from "src/domain/team/team-transformer";
import { KlawApiResponse } from "types/utils";

const getTeams = () => {
  return api
    .get<KlawApiResponse<"teamNamesGet">>("/getAllTeamsSUOnly")
    .then(transformTeamNamesGetResponse);
};

export { getTeams };
