import { NativeSelect, Option } from "@aivenio/design-system";
import { ChangeEvent } from "react";
import { Environment } from "src/domain/environment";

type SelectEnvProps = {
  environments: Array<{ label: string; value: Environment }>;
  activeOption: Environment;
  selectEnvironment: (value: Environment) => void;
};
function SelectEnv(props: SelectEnvProps) {
  const { environments, activeOption, selectEnvironment } = props;

  function onChangeEnv(event: ChangeEvent<HTMLSelectElement>) {
    selectEnvironment(event.target.value as Environment);
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
