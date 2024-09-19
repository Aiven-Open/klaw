import {
  Box,
  Button,
  EmptyState,
  NativeSelect,
  PageHeader,
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
  TopicMessagesFetchModeTypes,
  useMessagesFilters,
} from "src/app/features/topics/details/messages/useMessagesFilters";
import { getTopicMessages } from "src/domain/topic/topic-api";
import {
  type NoContent,
  type TopicMessages as TopicMessagesType,
  TOPIC_MESSAGE_DEFAULT_USER_GROUP_ID,
} from "src/domain/topic/topic-types";

function isNoContentResult(
  result: TopicMessagesType | NoContent | undefined
): result is NoContent {
  return Boolean(result && "status" in result);
}

function TopicMessages() {
  const { topicName, environmentId } = useTopicDetails();

  const {
    validateFilters,
    filterErrors,
    getFetchingMode,
    defaultOffsetFilters,
    customOffsetFilters,
    rangeOffsetFilters,
    partitionIdFilters,
  } = useMessagesFilters();

  const [fetchingMode, setFetchingMode] =
    useState<TopicMessagesFetchModeTypes>(getFetchingMode());

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
        consumerGroupId: TOPIC_MESSAGE_DEFAULT_USER_GROUP_ID,
        envId: environmentId,
        offsetId: defaultOffsetFilters.defaultOffset,
        selectedPartitionId: Number(partitionIdFilters.partitionId),
        selectedNumberOfOffsets: Number(customOffsetFilters.customOffset),
        selectedOffsetRangeStart: Number(rangeOffsetFilters.rangeOffsetStart),
        selectedOffsetRangeEnd: Number(rangeOffsetFilters.rangeOffsetEnd),
      }),
    keepPreviousData: true,
    refetchOnWindowFocus: true,
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

  function handleRangeOffsetStartChange(rangeOffsetStart: string): void {
    rangeOffsetFilters.setRangeOffsetStart(rangeOffsetStart);
  }

  function handleRangeOffsetEndChange(rangeOffsetEnd: string): void {
    rangeOffsetFilters.setRangeOffsetEnd(rangeOffsetEnd);
  }

  function handleFetchModeChange(
    selectedFetchMode: TopicMessagesFetchModeTypes
  ): void {
    if (fetchingMode === selectedFetchMode) {
      return;
    }
    if (selectedFetchMode === "default") {
      defaultOffsetFilters.setDefaultOffset("5");
    } else {
      defaultOffsetFilters.setDefaultOffset(selectedFetchMode);
    }
    setFetchingMode(selectedFetchMode);
  }

  function getMessagesUpdatedAt(): string {
    return new Intl.DateTimeFormat("UTC", {
      dateStyle: "short",
      timeStyle: "medium",
    }).format(messagesUpdatedAt);
  }

  function getButtonLabel(): string {
    if (fetchingMode === "custom") {
      return `Fetch and display the latest ${customOffsetFilters.customOffset} messages from partiton ${partitionIdFilters.partitionId} of topic ${topicName}`;
    }
    if (fetchingMode === "range") {
      return `Fetch and display the messages from offset ${rangeOffsetFilters.rangeOffsetStart} to offset ${rangeOffsetFilters.rangeOffsetEnd} from partiton ${partitionIdFilters.partitionId} of topic ${topicName}`;
    }
    return `Fetch and display the latest ${defaultOffsetFilters.defaultOffset} messages from topic ${topicName}`;
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
              <NativeSelect
                labelText={"Select mode"}
                description={"Choose mode to fetch messages"}
                key={"filter-fetch-mode-type"}
                defaultValue={getFetchingMode()}
                onChange={(event) => {
                  handleFetchModeChange(
                    event.target.value as TopicMessagesFetchModeTypes
                  );
                }}
              >
                <option key={"default"} value={"default"}>
                  {"Default"}
                </option>
                <option key={"custom"} value={"custom"}>
                  {"Custom"}
                </option>
                <option key={"range"} value={"range"}>
                  {"Range"}
                </option>
              </NativeSelect>

              <TopicMessageFilters
                key={"offset"}
                values={{
                  defaultOffset: defaultOffsetFilters.defaultOffset,
                  customOffset: customOffsetFilters.customOffset,
                  partitionId: partitionIdFilters.partitionId,
                  rangeOffsetStart: rangeOffsetFilters.rangeOffsetStart,
                  rangeOffsetEnd: rangeOffsetFilters.rangeOffsetEnd,
                }}
                disabled={isConsuming}
                onDefaultOffsetChange={handleDefaultOffsetChange}
                onPartitionIdChange={handlePartitionIdChange}
                onCustomOffsetChange={handleCustomOffsetChange}
                onRangeOffsetStartChange={handleRangeOffsetStartChange}
                onRangeOffsetEndChange={handleRangeOffsetEndChange}
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
                aria-label={getButtonLabel()}
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
