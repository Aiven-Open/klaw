import { useQuery } from "@tanstack/react-query";
import { getTeams } from "src/domain/team";
import { TeamsTable } from "src/app/features/configuration/teams/components/TeamsTable";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";

function Teams() {
  const {
    data: teams,
    isLoading,
    isError,
    error,
  } = useQuery(["get-teams"], {
    queryFn: () => getTeams(),
  });

  return (
    <TableLayout
      filters={[]}
      table={<TeamsTable teams={teams || []} />}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { Teams };
