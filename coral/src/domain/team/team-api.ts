import api, { API_PATHS } from "src/services/api";
import { KlawApiRequest, KlawApiResponse } from "types/utils";

const getTeams = () => {
  return api.get<KlawApiResponse<"getAllTeamsSU">>(API_PATHS.getAllTeamsSU);
};

const updateTeam = ({
  userName,
  teamId,
}: {
  userName: string;
  teamId: number;
}) => {
  const payload = {
    username: userName,
    teamId: teamId,
  };

  return api.post<
    KlawApiResponse<"updateUserTeamFromSwitchTeams">,
    KlawApiRequest<"updateUserTeamFromSwitchTeams">
    //@ts-ignore
  >("/user/updateTeam", payload);
};

const getTeamsOfUser = ({ userName }: { userName: string | undefined }) => {
  if (!userName) {
    return undefined;
  }
  return api.get<KlawApiResponse<"getSwitchTeams">>(
    //@ts-ignore
    `/user/${userName}/switchTeamsList`
  );
};

export { getTeams, getTeamsOfUser, updateTeam };
