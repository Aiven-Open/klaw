import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { getTopicRequests } from "src/domain/topic/topic-api";
import { TopicRequestsTable } from "src/app/features/requests/components/topics/components/TopicRequestsTable";

function TopicRequests() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["topicRequests"],
    queryFn: () =>
      getTopicRequests({
        pageNo: "1",
      }),
  });

  return (
    <TableLayout
      filters={[]}
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
