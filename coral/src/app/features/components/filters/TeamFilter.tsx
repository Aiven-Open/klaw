import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { useApiConfig } from "src/app/context-provider/ApiProvider";

function TeamFilter() {
  const apiConfig = useApiConfig();
  const { data: topicTeams } = useQuery(["get-teams"], {
    queryFn: () => apiConfig.getTeams(),
  });

  const { teamId, setFilterValue } = useFiltersContext();

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
