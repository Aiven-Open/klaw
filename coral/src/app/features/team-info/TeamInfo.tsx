import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getUserTeamName } from "src/domain/auth-user";
import { getTeamsOfUser } from "src/domain/team";
import { NativeSelect } from "@aivenio/aquarium";
import { updateTeam } from "src/domain/team/team-api";

function TeamInfo() {
  const queryClient = useQueryClient();

  const { data: user, isLoading: userLoading } = useQuery(
    ["user-getAuth-data"],
    getUserTeamName
  );

  const { data: teams, isLoading: teamsLoading } = useQuery(
    ["user-teams"],
    () => getTeamsOfUser({ userName: user?.userName }),
    {
      enabled: Boolean(user?.canSwitchTeams),
    }
  );

  const { mutate: updateTeamForUser } = useMutation(updateTeam, {
    onSuccess: async () => {
      queryClient.refetchQueries();
    },
    onError(error: Error) {
      console.error(error);
    },
  });

  if (userLoading) {
    return <i className="text-grey-40">Fetching team...</i>;
  }

  if ((!userLoading && !user) || !user?.teamName) {
    return <i>No team found</i>;
  }

  if (
    !user.canSwitchTeams ||
    (!teamsLoading && !teams) ||
    (teams && teams.length === 0)
  ) {
    return <>{user.teamName}</>;
  }

  return (
    <NativeSelect
      value={user.teamId}
      onChange={({ currentTarget: { value: teamId } }) => {
        console.log(teamId);
        updateTeamForUser({
          userName: user.userName,
          teamId: Number(teamId),
        });
      }}
    >
      {teams?.map((team) => {
        return (
          <option value={team.teamId} key={team.teamId}>
            {team.teamname}
          </option>
        );
      })}
    </NativeSelect>
  );
}

export { TeamInfo };
