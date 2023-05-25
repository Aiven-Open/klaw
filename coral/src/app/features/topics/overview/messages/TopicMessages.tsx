import { useQuery } from "@tanstack/react-query";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { TopicMessageList } from "src/app/features/topics/overview/messages/components/TopicMessageList";
import { getTopicMessages } from "src/domain/topic/topic-api";
import {
  type NoContent,
  type TopicMessages as TopicMessagesType,
} from "src/domain/topic/topic-types";
import { Box, Button, EmptyState } from "@aivenio/aquarium";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import refreshIcon from "@aivenio/aquarium/dist/src/icons/refresh";
import { TopicMessageOffsetFilter } from "src/app/features/topics/overview/messages/components/TopicMessageOffsetFilter";
import {
  Offset,
  useOffsetFilter,
} from "src/app/features/topics/overview/messages/useOffsetFilter";

function isNoContentResult(
  result: TopicMessagesType | NoContent | undefined
): result is NoContent {
  return Boolean(result && "status" in result);
}

function TopicMessages() {
  const { topicName } = useTopicDetails();
  const [offset, setOffset] = useOffsetFilter();
  const {
    data: consumeResult,
    isLoading,
    isError,
    isRefetching,
    error,
    refetch: updateResults,
  } = useQuery({
    queryKey: ["topicMessages", offset],
    queryFn: () =>
      getTopicMessages({
        topicName,
        consumerGroupId: "notdefined",
        envId: "2",
        offsetId: offset,
      }),
    keepPreviousData: false,
  });

  const isConsuming = isLoading || isRefetching;

  function handleUpdateResultClick(): void {
    updateResults();
  }

  function handleOffsetChange(offset: Offset): void {
    setOffset(offset);
  }

  return (
    <TableLayout
      filters={[
        <TopicMessageOffsetFilter
          key={"offset"}
          value={offset}
          disabled={isConsuming}
          onChange={handleOffsetChange}
        />,
        <Box.Flex key={"consume"} justifyContent="flex-end">
          <Button.Primary
            onClick={handleUpdateResultClick}
            disabled={isConsuming}
            loading={isConsuming}
            aria-label={`Consume and display the latest ${offset} messages from topic ${topicName}`}
            icon={refreshIcon}
          >
            Update results
          </Button.Primary>
        </Box.Flex>,
      ]}
      isLoading={isConsuming}
      isErrorLoading={isError}
      errorMessage={error}
      table={
        isNoContentResult(consumeResult) ? (
          <EmptyState title="No messages">
            This Topic contains no Messages.
          </EmptyState>
        ) : (
          <TopicMessageList messages={consumeResult ?? {}} />
        )
      }
    />
  );
}

export { TopicMessages };
