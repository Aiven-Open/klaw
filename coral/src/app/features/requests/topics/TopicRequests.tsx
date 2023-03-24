import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Pagination } from "src/app/components/Pagination";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import { MyRequestsFilter } from "src/app/features/components/table-filters/MyRequestsFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { useTableFiltersValues } from "src/app/features/components/table-filters/useTableFiltersValues";
import { TopicRequestsTable } from "src/app/features/requests/topics/components/TopicRequestsTable";
import { getTopicRequests } from "src/domain/topic/topic-api";

const defaultStatus = "ALL";

function TopicRequests() {
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { topic, environment, status, showOnlyMyRequests } =
    useTableFiltersValues();

  const { data, isLoading, isError, error } = useQuery({
    queryKey: [
      "topicRequests",
      currentPage,
      environment,
      status,
      topic,
      showOnlyMyRequests,
    ],
    queryFn: () =>
      getTopicRequests({
        pageNo: String(currentPage),
        // search is not yet implemented as a param to getTopicRequests
        // search: topic,
        env: environment,
        requestStatus: status,
        isMyRequest: showOnlyMyRequests,
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
        <EnvironmentFilter key="environments" />,
        <StatusFilter key="request-status" defaultStatus={defaultStatus} />,
        <TopicFilter key={"topic"} />,
        <MyRequestsFilter key={"isMyRequest"} />,
      ]}
      table={
        <TopicRequestsTable
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

export { TopicRequests };
