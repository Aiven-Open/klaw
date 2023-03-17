import { useQuery } from "@tanstack/react-query";
import { getSchemaRequests } from "src/domain/schema-request";
import { SchemaRequestTable } from "src/app/features/requests/components/schemas/components/SchemaRequestTable";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";

function SchemaRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? undefined;

  const {
    data: schemaRequests,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["schemaRequests", currentPage, currentTopic],
    queryFn: () =>
      getSchemaRequests({ pageNo: String(currentPage), topic: currentTopic }),
    keepPreviousData: true,
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
      filters={[<TopicFilter key={"topic"} />]}
      table={<SchemaRequestTable requests={schemaRequests?.entries || []} />}
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { SchemaRequests };
