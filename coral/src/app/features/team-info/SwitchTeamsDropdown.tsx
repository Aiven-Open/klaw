import { Box, Button, DropdownMenu, Skeleton } from "@aivenio/aquarium";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getTeamsOfUser, updateTeam } from "src/domain/team/team-api";
import { Dialog } from "src/app/components/Dialog";
import { useState } from "react";

type SwitchTeamsDropdownProps = {
  userName: string;
  currentTeam: string;
};

function SwitchTeamsDropdown({
  userName,
  currentTeam,
}: SwitchTeamsDropdownProps) {
  const queryClient = useQueryClient();

  const [modal, setModal] = useState<{
    open: boolean;
    newTeamId: number | null;
  }>({ open: false, newTeamId: null });

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
        //@ TODO error notification when we have toasts!
        console.error(error);
      },
      onSettled() {
        setModal({ open: false, newTeamId: null });
      },
    }
  );

  function changeTeams(teamId: number | null) {
    if (teamId) {
      updateTeamForUser({
        userName: userName,
        teamId: teamId,
      });
    }
  }

  if (teamsLoading) {
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
      {modal.open && (
        <Dialog
          title={"Switching teams"}
          primaryAction={{
            text: "Change team",
            onClick: () => changeTeams(modal.newTeamId),
            loading: updateIsLoading,
          }}
          secondaryAction={{
            text: "Cancel",
            onClick: () => setModal({ open: false, newTeamId: null }),
            disabled: updateIsLoading,
          }}
          type={"confirmation"}
        >
          You are updating the team you are signed in with.
        </Dialog>
      )}

      <DropdownMenu
        onAction={(newTeamId) =>
          setModal({ open: true, newTeamId: Number(newTeamId) })
        }
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
