import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { getTeams } from "src/domain/team/team-api";

type SelectTeamProps = {
  onChange: (teamId: string) => void;
};

function SelectTeam(props: SelectTeamProps) {
  const [searchParams, setSearchParams] = useSearchParams();

  const initialTeam = searchParams.get("team");
  const [team, setTeam] = useState<string>("ALL");
  const { onChange } = props;

  const { data: topicTeams } = useQuery(["topic-get-teams"], {
    queryFn: () => getTeams(),
    keepPreviousData: true,
  });

  function onChangeEnv(newTeam: string) {
    const isAllTeams = newTeam === "ALL";
    if (isAllTeams) {
      searchParams.delete("team");
    } else {
      searchParams.set("team", newTeam);
    }
    setTeam(newTeam);
    onChange(newTeam);
    setSearchParams(searchParams);
  }

  if (!topicTeams) {
    return (
      <div data-testid={"select-team-loading"}>
        <NativeSelect.Skeleton />
      </div>
    );
  } else {
    return (
      <NativeSelect
        labelText="Filter by team"
        value={initialTeam || team}
        onChange={(event) => onChangeEnv(event.target.value)}
      >
        <Option key={"ALL"} value={"ALL"}>
          All teams
        </Option>
        {topicTeams.map((team) => (
          <Option key={team.teamId} value={team.teamId}>
            {team.teamname}
          </Option>
        ))}
      </NativeSelect>
    );
  }
}

export default SelectTeam;
