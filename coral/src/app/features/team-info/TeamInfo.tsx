import { Box, Typography } from "@aivenio/aquarium";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { SwitchTeamsDropdown } from "src/app/features/team-info/SwitchTeamsDropdown";

function TeamInfo() {
  const authUser = useAuthContext();

  function renderTeamData() {
    if (!authUser) {
      return <></>;
    }

    if (authUser.canSwitchTeams !== "true") {
      return <>{authUser.teamname}</>;
    }

    return (
      <SwitchTeamsDropdown
        userName={authUser.username}
        currentTeam={authUser.teamname}
      />
    );
  }

  return (
    <Box
      display={"flex"}
      flexDirection={"column"}
      rowGap={"2"}
      height={"l4"}
      marginBottom={"l1"}
    >
      <Box display={"flex"} alignItems={"center"} colGap={"2"}>
        <Typography.SmallTextBold color={"grey-50"}>
          <span className={"visually-hidden"}>Your </span>
          Team
        </Typography.SmallTextBold>
      </Box>
      {renderTeamData()}
    </Box>
  );
}

export { TeamInfo };
