import { NativeSelect, Option } from "@aivenio/design-system";
import { useEffect, useState } from "react";
import { useGetTeams } from "src/app/features/topics/hooks/teams/useGetTeams";
import { Team } from "src/domain/team";
import { useSearchParams } from "react-router-dom";
import { ALL_TEAMS_VALUE } from "src/domain/team/team-types";

type SelectTeamProps = {
  updateTeam: (teamName: Team | null) => void;
};

function SelectTeam(props: SelectTeamProps) {
  const [searchParams, setSearchParams] = useSearchParams();

  const initialTeam = searchParams.get("team");
  const [team, setTeam] = useState<Team | null>(ALL_TEAMS_VALUE);
  const { updateTeam } = props;

  const { data: topicTeams } = useGetTeams();

  useEffect(() => {
    if (initialTeam) {
      setTeam(initialTeam);
    }
    // updates `team` in BrowseTopics
    // which will trigger the api call
    updateTeam(initialTeam || ALL_TEAMS_VALUE);
  }, [initialTeam]);

  function onChangeEnv(newTeam: string) {
    const isAllTeams = newTeam === ALL_TEAMS_VALUE;
    if (isAllTeams) {
      searchParams.delete("team");
    } else {
      searchParams.set("team", newTeam);
    }
    setTeam(newTeam);
    updateTeam(newTeam);
    setSearchParams(searchParams);
  }

  function createOptions(teams: Team[]) {
    return (
      <>
        <Option key={ALL_TEAMS_VALUE} value={ALL_TEAMS_VALUE}>
          All teams
        </Option>
        {teams.map((team) => (
          <Option key={team} value={team}>
            {team}
          </Option>
        ))}
      </>
    );
  }

  if (topicTeams && team) {
    return (
      <NativeSelect
        labelText="Team"
        value={team}
        onChange={(event) => onChangeEnv(event.target.value)}
      >
        {topicTeams && createOptions(topicTeams)}
      </NativeSelect>
    );
  } else {
    return <></>;
  }
}

export default SelectTeam;
