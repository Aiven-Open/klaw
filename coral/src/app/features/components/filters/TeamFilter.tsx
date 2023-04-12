import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { getTeams } from "src/domain/team/team-api";

function TeamFilter() {
  const { data: topicTeams } = useQuery(["topic-get-teams"], {
    queryFn: () => getTeams(),
  });

  const { teamId, setFilterValue } = useFiltersValues();

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
        value={teamId}
        onChange={(event) =>
          setFilterValue({ name: "teamId", value: event.target.value })
        }
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
