import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getTopicRequests } from "src/domain/topic/topic-api";
import { TopicRequestsTable } from "src/app/features/requests/topics/components/TopicRequestsTable";
import { useSearchParams } from "react-router-dom";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { Pagination } from "src/app/components/Pagination";
import { MyRequestsFilter } from "src/app/features/components/table-filters/MyRequestsFilter";
import { RequestStatus } from "src/domain/requests/requests-types";
import EnvironmentFilter from "src/app/features/components/table-filters/EnvironmentFilter";
import StatusFilter from "src/app/features/components/table-filters/StatusFilter";

const defaultStatus = "ALL";

function TopicRequests() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentTopic = searchParams.get("topic") ?? undefined;
  const currentEnvironment = searchParams.get("environment") ?? "ALL";
  const currentStatus =
    (searchParams.get("status") as RequestStatus) ?? defaultStatus;
  const showOnlyMyRequests =
    searchParams.get("showOnlyMyRequests") === "true" ? true : undefined;
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { data, isLoading, isError, error } = useQuery({
    queryKey: [
      "topicRequests",
      currentPage,
      currentEnvironment,
      currentStatus,
      currentTopic,
      showOnlyMyRequests,
    ],
    queryFn: () =>
      getTopicRequests({
        pageNo: String(currentPage),
        // search is not yet implemented as a param to getTopicRequests
        // search: currentTopic,
        env: currentEnvironment,
        requestStatus: currentStatus,
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
