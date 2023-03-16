import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getTopicRequests } from "src/domain/topic/topic-api";
import { TopicRequestsTable } from "src/app/features/requests/components/topics/components/TopicRequestsTable";
import { useSearchParams } from "react-router-dom";
import TopicFilter from "src/app/features/components/table-filters/TopicFilter";

function TopicRequests() {
  const [searchParams] = useSearchParams();
  const currentTopic = searchParams.get("topic") ?? "";

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["topicRequests", currentTopic],
    queryFn: () =>
      getTopicRequests({
        pageNo: "1",
        // search is not yet implemented as a param to getTopicRequests
        // search: currentTopic,
      }),
  });

  return (
    <TableLayout
      filters={[<TopicFilter key={"topic"} />]}
      table={
        <TopicRequestsTable
          requests={data?.entries ?? []}
          onDetails={() => null}
          onEdit={() => null}
          onDelete={() => null}
        />
      }
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
    />
  );
}

export { TopicRequests };
