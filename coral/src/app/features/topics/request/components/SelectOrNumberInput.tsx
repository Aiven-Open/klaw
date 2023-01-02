import isNumber from "lodash/isNumber";
import { NativeSelect, NumberInput } from "src/app/components/Form";
import { Schema } from "src/app/features/topics/request/schemas/topic-request-form";

type Props = {
  label: string;
  name: "topicpartitions" | "replicationfactor";
  max?: number;
};
const SelectOrNumberInput = ({ name, label, max }: Props) => {
  if (isNumber(max)) {
    return (
      <NativeSelect<Schema> name={name} labelText={label}>
        {[...Array(max).keys()]
          .map((i) => i + 1)
          .map((i) => (
            <option key={i} value={i}>
              {i}
            </option>
          ))}
      </NativeSelect>
    );
  } else {
    return (
      <NumberInput<Schema> name={name} labelText={label} min={1} max={max} />
    );
  }
};

export default SelectOrNumberInput;
