import api from "src/services/api";
import { KlawApiResponse } from "types/utils";

const getTeams = () => {
  return api.get<KlawApiResponse<"getAllTeamsSU">>("/getAllTeamsSU");
};

export { getTeams };
