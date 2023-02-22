import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { getTeams } from "src/domain/team/team-api";

type SelectTeamProps = {
  onChange: (teamId: string) => void;
};

function SelectTeam(props: SelectTeamProps) {
  const { onChange } = props;

  const [searchParams, setSearchParams] = useSearchParams();
  const team = searchParams.get("team");

  const { data: topicTeams } = useQuery(["topic-get-teams"], {
    queryFn: () => getTeams(),
    keepPreviousData: true,
  });

  function onChangeEnv(nextTeam: string) {
    const isAllTeams = nextTeam === "ALL";
    if (isAllTeams) {
      searchParams.delete("team");
    } else {
      searchParams.set("team", nextTeam);
    }
    onChange(nextTeam);
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
        value={team || "ALL"}
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
