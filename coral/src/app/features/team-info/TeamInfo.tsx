import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { NativeSelect } from "@aivenio/aquarium";
import { getTeamsOfUser, updateTeam } from "src/domain/team/team-api";
import { useAuthContext } from "src/app/context-provider/AuthProvider";

function TeamInfo() {
  const queryClient = useQueryClient();

  const authUser = useAuthContext();

  const { mutate: updateTeamForUser } = useMutation(updateTeam, {
    onSuccess: async () => {
      await queryClient.refetchQueries();
    },
    onError(error: Error) {
      console.error(error);
    },
  });

  const { data: teams, isLoading: teamsLoading } = useQuery(
    ["user-teams"],
    () => getTeamsOfUser({ userName: authUser?.username }),
    {
      enabled: Boolean(authUser && authUser?.canSwitchTeams),
    }
  );

  if (!authUser?.teamname || authUser.teamname.length === 0) {
    return <i>No team found</i>;
  }

  if (!authUser.canSwitchTeams) {
    return <>{authUser.teamname}</>;
  }

  console.log(authUser);

  if (teamsLoading) {
    console.log(teamsLoading);
    return <NativeSelect.Skeleton />;
  }

  return (
    <NativeSelect
      value={authUser.teamId}
      onChange={({ currentTarget: { value: teamId } }) => {
        console.log(teamId);
        updateTeamForUser({
          userName: authUser.username,
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
