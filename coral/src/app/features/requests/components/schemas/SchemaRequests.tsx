import { useQuery } from "@tanstack/react-query";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequestTable } from "src/app/features/requests/components/schemas/components/SchemaRequestTable";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";

function SchemaRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? undefined;

  const currentEnvironment = searchParams.get("environment") ?? "ALL";

  const {
    data: schemaRequests,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["schemaRequests", currentPage, currentEnvironment],
    queryFn: () =>
      getSchemaRequests({
        pageNo: String(currentPage),
        env: currentEnvironment,
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
        <EnvironmentFilter key={"environments"} isSchemaRegistryEnvironments />, <TopicFilter key={"topic"} />
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
