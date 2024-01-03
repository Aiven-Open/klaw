import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { ActivityLogTable } from "src/app/features/activity-log/components/ActivityLogTable";
import EnvironmentFilter from "src/app/features/components/filters/EnvironmentFilter";
import {
  useFiltersContext,
  withFiltersContext,
} from "src/app/features/components/filters/useFiltersContext";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getActivityLog } from "src/domain/requests/requests-api";

function ActivityLog() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { environment } = useFiltersContext();

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["getActivityLog", currentPage, environment],
    queryFn: () =>
      getActivityLog({
        pageNo: String(currentPage),
        env: environment === "ALL" ? undefined : environment,
      }),
    keepPreviousData: true,
    refetchOnWindowFocus: true,
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
    <>
      <TableLayout
        filters={[
          <EnvironmentFilter key="environment" environmentsFor="ALL" />,
        ]}
        table={
          <ActivityLogTable
            ariaLabel="Activity log"
            activityLogs={data?.entries ?? []}
          />
        }
        pagination={pagination}
        isLoading={isLoading}
        isErrorLoading={isError}
        errorMessage={error}
      />
    </>
  );
}

export default withFiltersContext({
  element: <ActivityLog />,
});
