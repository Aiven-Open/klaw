import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { TopicMessageList } from "src/app/features/topics/overview/messages/components/TopicMessageList";
import { getTopicMessages } from "src/domain/topic/topic-api";
import {
  type NoContent,
  type TopicMessages as TopicMessagesType,
} from "src/domain/topic/topic-types";
import { EmptyState } from "@aivenio/aquarium";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";

function isNoContentResult(
  result: TopicMessagesType | NoContent | undefined
): result is NoContent {
  return Boolean(result && "status" in result);
}

function TopicMessages() {
  const { topicName } = useTopicDetails();
  const {
    data: consumeResult,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["topicMessages"],
    queryFn: () =>
      getTopicMessages({
        topicName,
        consumerGroupId: "notdefined",
        envId: "2",
        offsetId: "5",
      }),
    keepPreviousData: true,
  });

  if (isNoContentResult(consumeResult)) {
    return (
      <EmptyState title="No messages">
        No Message matched your criteria.
      </EmptyState>
    );
  }

  return (
    <TableLayout
      filters={[]}
      isLoading={isLoading}
      isErrorLoading={isError}
      errorMessage={error}
      table={<TopicMessageList messages={consumeResult ?? {}} />}
    />
  );
}

export { TopicMessages };
