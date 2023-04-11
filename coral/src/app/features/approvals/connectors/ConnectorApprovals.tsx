import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import ConnectorApprovalsTable from "src/app/features/approvals/connectors/components/ConnectorApprovalsTable";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import { RequestTypeFilter } from "src/app/features/components/filters/RequestTypeFilter";
import SearchFilter from "src/app/features/components/filters/SearchFilter";
import StatusFilter from "src/app/features/components/filters/StatusFilter";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getConnectorRequestsForApprover } from "src/domain/connector";

const defaultType = "ALL";

function ConnectorApprovals() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { environment, status, search, requestType } = useFiltersValues({
    defaultStatus: "CREATED",
  });

  const {
    data: connectorRequests,
    isLoading: connectorRequestsIsLoading,
    isError: connectorRequestsIsError,
    error: connectorRequestsError,
  } = useQuery({
    queryKey: [
      "connectorRequestsForApprover",
      currentPage,
      status,
      environment,
      search,
      requestType,
    ],
    queryFn: () =>
      getConnectorRequestsForApprover({
        requestStatus: status,
        pageNo: currentPage.toString(),
        env: environment,
        search: search,
        operationType: requestType !== defaultType ? requestType : undefined,
      }),
    keepPreviousData: true,
  });

  const table = (
    <ConnectorApprovalsTable
      requests={connectorRequests?.entries || []}
      onDetails={() => null}
      onApprove={() => null}
      onDecline={() => null}
      isBeingDeclined={() => false}
      isBeingApproved={() => false}
      ariaLabel={`Connector approval requests, page ${
        connectorRequests?.currentPage ?? 0
      } of ${connectorRequests?.totalPages ?? 0}`}
    />
  );

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const pagination =
    connectorRequests?.totalPages && connectorRequests.totalPages > 1 ? (
      <Pagination
        activePage={connectorRequests.currentPage}
        totalPages={connectorRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  return (
    <TableLayout
      filters={[
        <EnvironmentFilter
          key={"environment"}
          environmentEndpoint={"getSyncConnectorsEnvironments"}
        />,
        <StatusFilter key={"status"} defaultStatus={"CREATED"} />,
        <RequestTypeFilter key={"requestType"} />,
        <SearchFilter key={"search"} />,
      ]}
      table={table}
      pagination={pagination}
      isLoading={connectorRequestsIsLoading}
      isErrorLoading={connectorRequestsIsError}
      errorMessage={connectorRequestsError}
    />
  );
}

export default ConnectorApprovals;
