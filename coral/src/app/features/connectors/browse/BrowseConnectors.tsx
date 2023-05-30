import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import ConnectorTable from "src/app/features/connectors/browse/components/ConnectorTable";
import { useQuery } from "@tanstack/react-query";
import { getConnectors } from "src/domain/connector/connector-api";
import { useSearchParams } from "react-router-dom";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { SearchConnectorFilter } from "src/app/features/components/filters/SearchConnectorFilter";
import TeamFilter from "src/app/features/components/filters/TeamFilter";

function BrowseConnectors() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const { environment, teamId, search } = useFiltersContext();

  const {
    data: connectors,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["browseConnectors", currentPage, environment, teamId, search],
    queryFn: () =>
      getConnectors({
        pageNo: currentPage.toString(),
        env: environment,
        connectornamesearch: search.length === 0 ? undefined : search,
        teamId: teamId !== "ALL" ? Number(teamId) : undefined,
      }),
    keepPreviousData: true,
  });

  function handleChangePage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }

  const pagination =
    connectors && connectors.totalPages > 1 ? (
      <Pagination
        activePage={connectors.currentPage}
        totalPages={connectors.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  return (
    <TableLayout
      filters={[
        <TeamFilter key={"team"} />,
        <EnvironmentFilter
          key={"environment"}
          environmentEndpoint={"getAllEnvironmentsForConnector"}
        />,
        <SearchConnectorFilter key="connector-name" />,
      ]}
      table={
        <ConnectorTable
          connectors={connectors?.entries ?? []}
          ariaLabel={`Connectors overview, page ${
            connectors?.currentPage ?? 0
          } of ${connectors?.totalPages ?? 0}`}
        />
      }
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export default withFiltersContext({
  element: <BrowseConnectors />,
});
