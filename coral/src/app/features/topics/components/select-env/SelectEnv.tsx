import { NativeSelect, Option } from "@aivenio/design-system";
import { ChangeEvent } from "react";
import { Environment } from "src/domain/environment";

type SelectEnvProps = {
  envOptions: Array<{ label: string; value: Environment }>;
  activeOption: Environment;
  selectEnv: (value: Environment) => void;
};
function SelectEnv(props: SelectEnvProps) {
  const { envOptions, activeOption, selectEnv } = props;

  function onChangeEnv(event: ChangeEvent<HTMLSelectElement>) {
    selectEnv(event.target.value as Environment);
  }

  return (
    <NativeSelect
      labelText="Kafka Environment"
      value={activeOption}
      onChange={(event) => onChangeEnv(event)}
    >
      {envOptions.map(({ label, value }) => (
        <Option key={value} value={value}>
          {label}
        </Option>
      ))}
    </NativeSelect>
  );
}

export default SelectEnv;
