import {
  Box,
  Input,
  RadioButton,
  RadioButtonGroup,
  RadioButtonGroupProps,
} from "@aivenio/aquarium";
import {
  defaultOffsets,
  type DefaultOffset,
  type FilterErrors,
} from "src/app/features/topics/details/messages/useMessagesFilters";

type Props = {
  values: {
    defaultOffset: DefaultOffset;
    customOffset: string | null;
    partitionId: string | null;
  };
  disabled?: RadioButtonGroupProps["disabled"];
  onDefaultOffsetChange: (defaultOffset: DefaultOffset) => void;
  onPartitionIdChange: (partitionId: string) => void;
  onCustomOffsetChange: (customOffset: string) => void;
  mode: "Default" | "Custom";
  filterErrors: FilterErrors;
};

function TopicMessageFilters({
  values,
  disabled = false,
  onDefaultOffsetChange,
  onPartitionIdChange,
  onCustomOffsetChange,
  mode,
  filterErrors,
}: Props) {
  return mode === "Default" ? (
    <RadioButtonGroup
      name={"defaultOffset"}
      value={values.defaultOffset}
      labelText="Number of messages"
      disabled={disabled}
      onChange={(value) => onDefaultOffsetChange(value as DefaultOffset)}
      description={
        "Choose how many recent messages you want to view from this topic."
      }
    >
      {defaultOffsets.map((defaultOffset) => {
        if (defaultOffset === "custom") {
          return;
        }
        return (
          <RadioButton key={defaultOffset} value={defaultOffset}>
            {defaultOffset}
          </RadioButton>
        );
      })}
    </RadioButtonGroup>
  ) : (
    <Box.Flex gap={"l1"}>
      <Input
        value={values.partitionId || undefined}
        labelText="Partition ID"
        placeholder={"1"}
        description={"Choose which partition you want to query."}
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
          "Choose how many recent messages you want to view from this partition."
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

export { TopicMessageFilters };
