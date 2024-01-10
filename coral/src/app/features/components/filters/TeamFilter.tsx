import { NativeSelect, Option } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { getTeams } from "src/domain/team/team-api";
import { AsyncNativeSelectWrapper } from "src/app/components/AsyncNativeSelectWrapper";

type TeamFilterProps = {
  useTeamName?: boolean;
};
function TeamFilter({ useTeamName }: TeamFilterProps) {
  const {
    data: teams,
    isLoading,
    isError,
    error,
  } = useQuery(["get-teams"], {
    queryFn: () => getTeams(),
  });

  const { teamId, teamName, setFilterValue } = useFiltersContext();

  return (
    <AsyncNativeSelectWrapper
      entity={"Teams"}
      isLoading={isLoading}
      isError={isError}
      error={error}
    >
      <NativeSelect
        labelText="Filter by team"
        value={useTeamName ? teamName : teamId}
        onChange={(event) => {
          return setFilterValue({
            name: useTeamName ? "teamName" : "teamId",
            value: event.target.value,
          });
        }}
      >
        <Option key={"ALL"} value={"ALL"}>
          All teams
        </Option>
        {teams?.map((team) => (
          <Option
            key={team.teamId}
            value={useTeamName ? team.teamname : team.teamId}
          >
            {team.teamname}
          </Option>
        ))}
      </NativeSelect>
    </AsyncNativeSelectWrapper>
  );
}

export default TeamFilter;
