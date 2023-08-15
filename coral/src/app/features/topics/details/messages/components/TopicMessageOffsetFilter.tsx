import {
  RadioButton,
  RadioButtonGroup,
  RadioButtonGroupProps,
} from "@aivenio/aquarium";
import {
  type Offset,
  offsets,
} from "src/app/features/topics/details/messages/useOffsetFilter";

type Props = {
  value: Offset;
  disabled?: RadioButtonGroupProps["disabled"];
  onChange: (offset: Offset) => void;
};

function TopicMessageOffsetFilter({
  value,
  disabled = false,
  onChange,
}: Props) {
  return (
    <RadioButtonGroup
      name={"offset"}
      value={value}
      labelText="Number of messages"
      disabled={disabled}
      onChange={(value) => onChange(value as Offset)}
      description={
        "Choose how many recent messages you want to view from this topic."
      }
    >
      {offsets.map((offset) => (
        <RadioButton key={offset} value={offset}>
          {offset}
        </RadioButton>
      ))}
    </RadioButtonGroup>
  );
}

export { TopicMessageOffsetFilter };
