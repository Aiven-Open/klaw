import api, { API_PATHS, DYNAMIC_API_PATHS } from "src/services/api";
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
    // The openapi definition claims that it needs the full UserModel
    // but for this it only needs username and teamId. We don't have
    // the full UserModel available in coral and
    // since it contains passwords we decided against that.
    // follow up in BE needed for this
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
  >(API_PATHS.updateUserTeamFromSwitchTeams, payload);
};

const getTeamsOfUser = ({ userName }: { userName: string }) => {
  return api.get<KlawApiResponse<"getSwitchTeams">>(
    DYNAMIC_API_PATHS.getSwitchTeams({ userId: userName })
  );
};

export { getTeams, getTeamsOfUser, updateTeam };
