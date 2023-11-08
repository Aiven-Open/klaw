import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useQuery } from "@tanstack/react-query";
import { getUserList } from "src/domain/user";
import { UsersTable } from "src/app/features/configuration/users/components/UsersTable";
import { Pagination } from "src/app/components/Pagination";
import { useSearchParams } from "react-router-dom";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import TeamFilter from "src/app/features/components/filters/TeamFilter";

function UsersWithoutFilterContext() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { teamName } = useFiltersContext();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const {
    data: users,
    isLoading,
    isError,
    error,
  } = useQuery(["get-user-list", currentPage, teamName], {
    queryFn: () =>
      getUserList({
        pageNo: String(currentPage),
        teamName: teamName === "ALL" ? undefined : teamName,
      }),
    keepPreviousData: true,
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
      filters={[<TeamFilter useTeamName={true} key={"team"} />]}
      table={table}
      pagination={pagination}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

const Users = withFiltersContext({
  element: <UsersWithoutFilterContext />,
});

export { Users };
