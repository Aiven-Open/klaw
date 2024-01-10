import { Box, Typography } from "@aivenio/aquarium";
import { useAuthContext } from "src/app/context-provider/AuthProvider";
import { SwitchTeamsDropdown } from "src/app/features/team-info/SwitchTeamsDropdown";

function TeamInfo() {
  const {
    authUser: { canSwitchTeams, teamname, username },
  } = useAuthContext();

  function renderTeamData() {
    if (canSwitchTeams !== "true") {
      return <>{teamname}</>;
    }

    return <SwitchTeamsDropdown userName={username} currentTeam={teamname} />;
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
        <Typography.SmallStrong color={"grey-50"}>
          <span className={"visually-hidden"}>Your </span>
          Team
        </Typography.SmallStrong>
      </Box>
      {renderTeamData()}
    </Box>
  );
}

export { TeamInfo };
