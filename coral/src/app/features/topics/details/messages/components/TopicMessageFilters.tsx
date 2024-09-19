import {
  Box,
  Input,
  RadioButton,
  RadioButtonGroup,
  RadioButtonGroupProps,
} from "@aivenio/aquarium";
import {
  type DefaultOffset,
  type FilterErrors,
} from "src/app/features/topics/details/messages/useMessagesFilters";
import {
  defaultOffsets,
  TopicMessagesFetchModeTypes,
} from "src/domain/topic/topic-types";

type Props = {
  values: {
    defaultOffset: DefaultOffset;
    customOffset: string | null;
    partitionId: string | null;
    rangeOffsetStart: string | null;
    rangeOffsetEnd: string | null;
  };
  disabled?: RadioButtonGroupProps["disabled"];
  onDefaultOffsetChange: (defaultOffset: DefaultOffset) => void;
  onPartitionIdChange: (partitionId: string) => void;
  onCustomOffsetChange: (customOffset: string) => void;
  onRangeOffsetStartChange: (rangeOffsetStart: string) => void;
  onRangeOffsetEndChange: (rangeOffsetEnd: string) => void;
  mode: TopicMessagesFetchModeTypes;
  filterErrors: FilterErrors;
};

function TopicMessageFilters({
  values,
  disabled = false,
  onDefaultOffsetChange,
  onPartitionIdChange,
  onCustomOffsetChange,
  onRangeOffsetStartChange,
  onRangeOffsetEndChange,
  mode,
  filterErrors,
}: Props) {
  if (mode === "default") {
    return (
      <RadioButtonGroup
        name={"defaultOffset"}
        value={values.defaultOffset}
        labelText="Number of messages"
        disabled={disabled}
        onChange={(value) => onDefaultOffsetChange(value as DefaultOffset)}
        description={"Select number of messages to display from this topic"}
      >
        {defaultOffsets.map((defaultOffset) => {
          if (defaultOffset === "custom" || defaultOffset === "range") {
            return;
          }
          return (
            <RadioButton key={defaultOffset} value={defaultOffset}>
              {defaultOffset}
            </RadioButton>
          );
        })}
      </RadioButtonGroup>
    );
  }

  if (mode === "custom") {
    return (
      <Box.Flex gap={"l1"}>
        <Input
          value={values.partitionId || undefined}
          labelText="Partition ID"
          placeholder={"0"}
          description={"Enter partition ID to retrieve last messages"}
          onChange={(e) => onPartitionIdChange(e.target.value)}
          type="number"
          helperText={filterErrors.partitionIdFilters || undefined}
          valid={filterErrors.partitionIdFilters === null}
          required
        />
        <Input
          value={values.customOffset || undefined}
          labelText="Number of messages"
          placeholder={"50"}
          description={
            "Set the number of recent messages to display from this partition"
          }
          onChange={(e) => onCustomOffsetChange(e.target.value)}
          type="number"
          helperText={filterErrors.customOffsetFilters || undefined}
          valid={filterErrors.customOffsetFilters === null}
          required
        />
      </Box.Flex>
    );
  }

  return (
    <Box.Flex gap={"l1"}>
      <Input
        value={values.partitionId || undefined}
        labelText="Partition ID"
        placeholder={"0"}
        description={"Enter partition ID to retrieve last messages"}
        onChange={(e) => onPartitionIdChange(e.target.value)}
        type="number"
        helperText={filterErrors.partitionIdFilters || undefined}
        valid={filterErrors.partitionIdFilters === null}
        required
      />
      <>
        {
          //Empty node prevents stale value of custom's 'Number of messages'
          //being populated into range's 'Start Offset' and vice versa
        }
      </>
      <Input
        value={values.rangeOffsetStart || undefined}
        labelText="Start Offset"
        placeholder={"0"}
        description={"Set the start offset"}
        onChange={(e) => onRangeOffsetStartChange(e.target.value)}
        type="number"
        helperText={filterErrors.rangeOffsetStartFilters || undefined}
        valid={filterErrors.rangeOffsetStartFilters === null}
        required
      />
      <Input
        value={values.rangeOffsetEnd || undefined}
        labelText="End Offset"
        placeholder={"10"}
        description={"Set the end offset"}
        onChange={(e) => onRangeOffsetEndChange(e.target.value)}
        type="number"
        helperText={filterErrors.rangeOffsetEndFilters || undefined}
        valid={filterErrors.rangeOffsetEndFilters === null}
        required
      />
    </Box.Flex>
  );
}

export { TopicMessageFilters };
