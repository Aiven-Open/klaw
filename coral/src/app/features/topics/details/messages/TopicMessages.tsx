import {
  Box,
  Button,
  EmptyState,
  PageHeader,
  Switch,
  SwitchGroup,
  Typography,
} from "@aivenio/aquarium";
import refreshIcon from "@aivenio/aquarium/dist/src/icons/refresh";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { TableLayout } from "src/app/features/components/layouts/TableLayout";
import { useTopicDetails } from "src/app/features/topics/details/TopicDetails";
import { TopicMessageFilters } from "src/app/features/topics/details/messages/components/TopicMessageFilters";
import { TopicMessageList } from "src/app/features/topics/details/messages/components/TopicMessageList";
import {
  DefaultOffset,
  useMessagesFilters,
} from "src/app/features/topics/details/messages/useMessagesFilters";
import { getTopicMessages } from "src/domain/topic/topic-api";
import {
  type NoContent,
  type TopicMessages as TopicMessagesType,
} from "src/domain/topic/topic-types";

function isNoContentResult(
  result: TopicMessagesType | NoContent | undefined
): result is NoContent {
  return Boolean(result && "status" in result);
}

function TopicMessages() {
  const { topicName } = useTopicDetails();

  const {
    validateFilters,
    filterErrors,
    getFetchingMode,
    defaultOffsetFilters,
    customOffsetFilters,
    partitionIdFilters,
  } = useMessagesFilters();

  const [fetchingMode, setFetchingMode] = useState<"Default" | "Custom">(
    getFetchingMode()
  );

  const {
    data: consumeResult,
    isError,
    isRefetching,
    isInitialLoading,
    error,
    refetch: updateResults,
    dataUpdatedAt: messagesUpdatedAt,
  } = useQuery({
    enabled: false, // No initial fetch, only fetch when refetch is called
    queryKey: ["topicMessages"],
    queryFn: () =>
      getTopicMessages({
        topicName,
        consumerGroupId: "notdefined",
        envId: "2",
        offsetId: defaultOffsetFilters.defaultOffset,
        selectedPartitionId: Number(partitionIdFilters.partitionId),
        selectedNumberOfOffsets: Number(customOffsetFilters.customOffset),
      }),
    keepPreviousData: true,
  });

  const isConsuming = isInitialLoading || isRefetching;

  function handleUpdateResultClick(): void {
    const isValid = validateFilters();

    if (isValid) {
      updateResults();
    }
  }

  function handleDefaultOffsetChange(offset: DefaultOffset): void {
    defaultOffsetFilters.setDefaultOffset(offset);
  }

  function handlePartitionIdChange(partitionId: string): void {
    partitionIdFilters.setPartitionId(partitionId);
  }

  function handleCustomOffsetChange(customOffset: string): void {
    customOffsetFilters.setCustomOffset(customOffset);
  }

  function handleFetchModeChange(): void {
    if (fetchingMode === "Default") {
      setFetchingMode("Custom");
      defaultOffsetFilters.setDefaultOffset("custom");
    } else {
      setFetchingMode("Default");
      defaultOffsetFilters.setDefaultOffset("5");
    }
  }

  function getMessagesUpdatedAt(): string {
    return new Intl.DateTimeFormat("UTC", {
      dateStyle: "short",
      timeStyle: "medium",
    }).format(messagesUpdatedAt);
  }

  function getTableContent() {
    if (!consumeResult) {
      return (
        <EmptyState title="No messages displayed">
          To view messages in this topic, select the number of messages
          you&apos;d like to view and select Fetch messages.
        </EmptyState>
      );
    } else if (isNoContentResult(consumeResult)) {
      return (
        <EmptyState title="No messages">
          This topic contains no messages.
        </EmptyState>
      );
    } else {
      return <TopicMessageList messages={consumeResult ?? {}} />;
    }
  }

  return (
    <>
      <PageHeader
        title="Messages"
        subtitle={
          messagesUpdatedAt ? (
            <Typography.Caption>
              <i>Last updated {getMessagesUpdatedAt()}</i>
            </Typography.Caption>
          ) : (
            <Box height={"l1"} />
          )
        }
      />
      <TableLayout
        filters={[
          <Box.Flex key="things" justifyContent="space-between" height={"l6"}>
            <Box.Flex gap="l2">
              <SwitchGroup
                key="fetchingMode"
                labelText="Fetching mode"
                description={
                  fetchingMode === "Default"
                    ? "Select message offset"
                    : "Specify message offset"
                }
              >
                <Switch
                  onChange={handleFetchModeChange}
                  checked={fetchingMode === "Custom"}
                >
                  {fetchingMode}
                </Switch>
              </SwitchGroup>

              <TopicMessageFilters
                key={"offset"}
                values={{
                  defaultOffset: defaultOffsetFilters.defaultOffset,
                  customOffset: customOffsetFilters.customOffset,
                  partitionId: partitionIdFilters.partitionId,
                }}
                disabled={isConsuming}
                onDefaultOffsetChange={handleDefaultOffsetChange}
                onPartitionIdChange={handlePartitionIdChange}
                onCustomOffsetChange={handleCustomOffsetChange}
                mode={fetchingMode}
                filterErrors={filterErrors}
              />
            </Box.Flex>
            <Box>
              <Button.Primary
                key="button"
                onClick={handleUpdateResultClick}
                disabled={isConsuming}
                loading={isConsuming}
                aria-label={
                  fetchingMode === "Default"
                    ? `Fetch and display the latest ${defaultOffsetFilters.defaultOffset} messages from topic ${topicName}`
                    : `Fetch and display the latest ${customOffsetFilters.customOffset} messages from partiton ${partitionIdFilters.partitionId} of topic ${topicName}`
                }
                icon={refreshIcon}
              >
                Fetch messages
              </Button.Primary>
            </Box>
          </Box.Flex>,
        ]}
        isLoading={isConsuming}
        isErrorLoading={isError}
        errorMessage={error}
        table={getTableContent()}
      />
    </>
  );
}

export { TopicMessages };
