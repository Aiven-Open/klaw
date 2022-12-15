import { NativeSelect, Option } from "@aivenio/aquarium";
import { useEffect, useState } from "react";
import { useGetTeams } from "src/app/features/browse-topics/hooks/teams/useGetTeams";
import { Team, TEAM_NOT_INITIALIZED } from "src/domain/team";
import { useSearchParams } from "react-router-dom";
import { ALL_TEAMS_VALUE } from "src/domain/team/team-types";

type SelectTeamProps = {
  onChange: (teamName: Team) => void;
};

function SelectTeam(props: SelectTeamProps) {
  const [searchParams, setSearchParams] = useSearchParams();

  const initialTeam = searchParams.get("team");
  const [team, setTeam] = useState<Team>(TEAM_NOT_INITIALIZED);
  const { onChange } = props;

  const { data: topicTeams } = useGetTeams();

  useEffect(() => {
    if (initialTeam) {
      setTeam(initialTeam);
    }
    // updates `team` in BrowseTopics
    // which will trigger the api call
    onChange(initialTeam || ALL_TEAMS_VALUE);
  }, [initialTeam]);

  function onChangeEnv(newTeam: string) {
    const isAllTeams = newTeam === ALL_TEAMS_VALUE;
    if (isAllTeams) {
      searchParams.delete("team");
    } else {
      searchParams.set("team", newTeam);
    }
    setTeam(newTeam);
    onChange(newTeam);
    setSearchParams(searchParams);
  }

  if (!topicTeams || !team) {
    return (
      <div data-testid={"select-team-loading"}>
        <NativeSelect.Skeleton />
      </div>
    );
  } else {
    return (
      <NativeSelect
        labelText="Filter by team"
        value={team}
        onChange={(event) => onChangeEnv(event.target.value)}
      >
        <Option key={ALL_TEAMS_VALUE} value={ALL_TEAMS_VALUE}>
          All teams
        </Option>
        {topicTeams.map((team) => (
          <Option key={team} value={team}>
            {team}
          </Option>
        ))}
      </NativeSelect>
    );
  }
}

export default SelectTeam;
