import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { AclRequestsTable } from "src/app/features/requests/components/acls/components/AclRequestsTable";
import { getAclRequests } from "src/domain/acl/acl-api";

function AclRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? "";

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["aclRequests", currentPage, currentTopic],
    queryFn: () =>
      getAclRequests({
        pageNo: String(currentPage),
        topic: currentTopic,
      }),
    keepPreviousData: true,
  });

  const handleChangePage = (page: number) => {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  };

  const pagination =
    data?.totalPages && data.totalPages > 1 ? (
      <Pagination
        activePage={data.currentPage}
        totalPages={data.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  return (
    <TableLayout
      filters={[<TopicFilter key="search" />]}
      table={
        <AclRequestsTable
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

export { AclRequests };
