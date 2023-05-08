import { Box, Button, DropdownMenu, Skeleton } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getTeamsOfUser, updateTeam } from "src/domain/team/team-api";

type SwitchTeamsDropdownProps = {
  userName: string;
  currentTeam: string;
};

function SwitchTeamsDropdown({
  userName,
  currentTeam,
}: SwitchTeamsDropdownProps) {
  const queryClient = useQueryClient();

  const { data: teams, isLoading: teamsLoading } = useQuery(
    ["user-teams"],
    () => getTeamsOfUser({ userName: userName })
  );

  const { mutate: updateTeamForUser, isLoading: updateIsLoading } = useMutation(
    updateTeam,
    {
      onSuccess: async () => {
        await queryClient.refetchQueries();
      },
      onError(error: Error) {
        console.error(error);
      },
    }
  );

  if (teamsLoading || updateIsLoading) {
    return (
      <div data-testid={"teams-loading"}>
        <Skeleton />
      </div>
    );
  }

  if (!teams || teams.length <= 1) {
    return <>{currentTeam}</>;
  }

  return (
    <>
      <DropdownMenu
        onAction={(teamId) => {
          updateTeamForUser({
            userName: userName,
            teamId: Number(teamId),
          });
        }}
      >
        <DropdownMenu.Trigger>
          <Button.SecondaryDropdown dense>
            <Box width={"l7"} display={"flex"} alignContent={"left"}>
              <span className={"visually-hidden"}>Change your team</span>
              <span aria-hidden={"true"}>{currentTeam}</span>
            </Box>
          </Button.SecondaryDropdown>
        </DropdownMenu.Trigger>
        <DropdownMenu.Items>
          {teams.map((team) => {
            return (
              <DropdownMenu.Item key={team.teamId}>
                {team.teamname}
              </DropdownMenu.Item>
            );
          })}
        </DropdownMenu.Items>
      </DropdownMenu>
    </>
  );
}

export { SwitchTeamsDropdown };
