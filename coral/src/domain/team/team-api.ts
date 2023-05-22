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
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
  >(API_PATHS.updateUserTeamFromSwitchTeams, payload);
};

const getTeamsOfUser = ({ userName }: { userName: string }) => {
  return api.get<KlawApiResponse<"getSwitchTeams">>(
    // API_PATH does not cover arguments,
    // follow up ticket: https://github.com/aiven/klaw/issues/1021
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    `/user/${userName}/switchTeamsList`
  );
};

export { getTeams, getTeamsOfUser, updateTeam };
