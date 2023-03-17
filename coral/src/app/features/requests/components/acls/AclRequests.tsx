import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import AclTypeFilter from "src/app/features/components/table-filters/AclTypeFilter";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { AclRequestsTable } from "src/app/features/requests/components/acls/components/AclRequestsTable";
import { getAclRequests } from "src/domain/acl/acl-api";
import { AclRequest } from "src/domain/acl/acl-types";

function AclRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;
  const currentTopic = searchParams.get("topic") ?? "";
  const currentEnvironment = searchParams.get("environment") ?? "ALL";
  const currentAclType =
    (searchParams.get("aclType") as AclRequest["aclType"]) ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as AclRequest["requestStatus"]) ?? "ALL";

  const { data, isLoading, isError, error } = useQuery({
    queryKey: [
      "aclRequests",
      currentPage,
      currentTopic,
      currentEnvironment,
      currentAclType,
      currentStatus,
    ],
    queryFn: () =>
      getAclRequests({
        pageNo: String(currentPage),
        topic: currentTopic,
        env: currentEnvironment,
        aclType: currentAclType,
        requestStatus: currentStatus,
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
      filters={[
        <EnvironmentFilter key="environment" />,
        <AclTypeFilter key="aclType" />,
        <StatusFilter key="status" defaultStatus="ALL" />,
        <TopicFilter key="search" />,
      ]}
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
