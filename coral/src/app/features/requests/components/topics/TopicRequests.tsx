import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getTopicRequests } from "src/domain/topic/topic-api";
import { TopicRequestsTable } from "src/app/features/requests/components/topics/components/TopicRequestsTable";
import { useSearchParams } from "react-router-dom";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";
import { Pagination } from "src/app/components/Pagination";
import { MyRequestFilter } from "src/app/features/components/table-filters/MyRequestFilter";

function TopicRequests() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentTopic = searchParams.get("topic") ?? undefined;
  const isMyRequest =
    searchParams.get("isMyRequest") === "true" ? true : undefined;
  const currentPage = searchParams.get("page")
    ? Number(searchParams.get("page"))
    : 1;

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["topicRequests", currentTopic, currentPage, isMyRequest],
    queryFn: () =>
      getTopicRequests({
        pageNo: String(currentPage),
        // search is not yet implemented as a param to getTopicRequests
        // search: currentTopic,
        isMyRequest,
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
        <TopicFilter key={"topic"} />,
        <MyRequestFilter key={"isMyRequest"} />,
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
