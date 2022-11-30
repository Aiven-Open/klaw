import { NativeSelect, Option } from "@aivenio/design-system";
import { ChangeEvent } from "react";

type SelectEnvProps = {
  environments: Array<{ label: string; value: string }>;
  activeOption: string;
  selectEnvironment: (value: string) => void;
};
function SelectEnv(props: SelectEnvProps) {
  const { environments, activeOption, selectEnvironment } = props;

  function onChangeEnv(event: ChangeEvent<HTMLSelectElement>) {
    selectEnvironment(event.target.value);
  }

  return (
    <NativeSelect
      labelText="Kafka Environment"
      value={activeOption}
      onChange={(event) => onChangeEnv(event)}
    >
      {environments.map(({ label, value }) => (
        <Option key={value} value={value}>
          {label}
        </Option>
      ))}
    </NativeSelect>
  );
}

export default SelectEnv;
