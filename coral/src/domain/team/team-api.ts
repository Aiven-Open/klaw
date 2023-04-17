import api, { API_PATHS } from "src/services/api";
import { KlawApiResponse } from "types/utils";

const getTeams = () => {
  return api.get<KlawApiResponse<"getAllTeamsSU">>(API_PATHS.getAllTeamsSU);
};

export { getTeams };
