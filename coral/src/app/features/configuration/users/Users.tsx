import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useQuery } from "@tanstack/react-query";
import { getUserList } from "src/domain/user";
import { UsersTable } from "src/app/features/configuration/users/components/UsersTable";
import { Pagination } from "src/app/components/Pagination";
import { useSearchParams } from "react-router-dom";

function Users() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const {
    data: users,
    isLoading,
    isError,
    error,
  } = useQuery(["get-user-list", currentPage], {
    queryFn: () => getUserList({ pageNo: String(currentPage) }),
  });

  function handleChangePage(page: number) {
    searchParams.set("page", page.toString());
    setSearchParams(searchParams);
  }

  const pagination =
    users && users.totalPages > 1 ? (
      <Pagination
        activePage={users.currentPage}
        totalPages={users.totalPages}
        setActivePage={handleChangePage}
      />
    ) : undefined;

  const table = (
    <UsersTable
      users={users?.entries || []}
      ariaLabel={`Users overview, page ${users?.currentPage ?? 0} of ${
        users?.totalPages ?? 0
      }`}
    />
  );

  return (
    <TableLayout
      filters={[]}
      table={table}
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { Users };
