import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { ConnectorRequestsTable } from "src/app/features/requests/connectors/components/ConnectorRequestsTable";
import { getConnectorRequests } from "src/domain/connector";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import SearchFilter from "src/app/features/components/filters/SearchFilter";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";

const defaultStatus = "ALL";

function ConnectorRequests() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { search, environment, status, showOnlyMyRequests } =
    useFiltersValues();

  const { data, isLoading, isError, error } = useQuery({
    queryKey: [
      "connectorRequests",
      currentPage,
      search,
      environment,
      showOnlyMyRequests,
      status,
    ],
    queryFn: () =>
      getConnectorRequests({
        pageNo: String(currentPage),
        isMyRequest: showOnlyMyRequests,
        env: environment,
        search,
        requestStatus: status,
      }),
    keepPreviousData: true,
  });

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const pagination =
    data?.totalPages && data.totalPages > 1 ? (
      <Pagination
        activePage={data.currentPage}
        totalPages={data?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  return (
    <TableLayout
      filters={[
        <SearchFilter key="connector" />,
        <EnvironmentFilter
          key="environment"
          environmentEndpoint="getSyncConnectorsEnvironments"
        />,
        <StatusFilter key="request-status" defaultStatus={defaultStatus} />,
        <MyRequestsFilter key={"isMyRequest"} />,
      ]}
      table={
        <ConnectorRequestsTable
          ariaLabel="Connector requests"
          requests={data?.entries ?? []}
          onDetails={() => null}
          onDelete={() => null}
        />
      }
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { ConnectorRequests };
