import { useQuery } from "@tanstack/react-query";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequestTable } from "src/app/features/requests/components/schemas/components/SchemaRequestTable";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import { RequestStatus } from "src/domain/requests/requests-types";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";

const defaultStatus = "ALL";

function SchemaRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? undefined;

  const currentEnvironment = searchParams.get("environment") ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as RequestStatus) ?? defaultStatus;

  const {
    data: schemaRequests,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: [
      "schemaRequests",
      currentPage,
      currentEnvironment,
      currentStatus,
    ],
    queryFn: () =>
      getSchemaRequests({
        pageNo: String(currentPage),
        env: currentEnvironment,
        requestStatus: currentStatus,
        topic: currentTopic
      }),
  });

  const setCurrentPage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const pagination =
    schemaRequests?.totalPages && schemaRequests.totalPages > 1 ? (
      <Pagination
        activePage={schemaRequests.currentPage}
        totalPages={schemaRequests?.totalPages}
        setActivePage={setCurrentPage}
      />
    ) : undefined;

  return (
    <TableLayout
      filters={[
        <EnvironmentFilter key={"environments"} isSchemaRegistryEnvironments />,
        <StatusFilter key={"request-status"} defaultStatus={defaultStatus} />,
        <TopicFilter key={"topic"} />
        ,
      ]}
      table={<SchemaRequestTable requests={schemaRequests?.entries || []} />}
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { SchemaRequests };
