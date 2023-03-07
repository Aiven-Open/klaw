import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { getTeams } from "src/domain/team/team-api";

function TeamFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const team = searchParams.get("team") ?? "ALL";

  const { data: topicTeams } = useQuery(["topic-get-teams"], {
    queryFn: () => getTeams(),
  });

  const handleChangeTeam = (nextTeamId: string) => {
    const isAllTeams = nextTeamId === "ALL";
    if (isAllTeams) {
      searchParams.delete("team");
      searchParams.set("page", "1");
    } else {
      searchParams.set("team", nextTeamId);
      searchParams.set("page", "1");
    }
    setSearchParams(searchParams);
  };

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
        value={team}
        onChange={(event) => handleChangeTeam(event.target.value)}
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

export default TeamFilter;
