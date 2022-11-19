import { NativeSelect, Option } from "@aivenio/design-system";
import { ChangeEvent } from "react";
import { TopicEnv } from "src/domain/topics";

type SelectEnvProps = {
  envOptions: Array<{ label: string; value: string }>;
  activeOption: TopicEnv;
  selectEnv: (value: TopicEnv) => void;
};
function SelectEnv(props: SelectEnvProps) {
  const { envOptions, activeOption, selectEnv } = props;

  function onChangeEnv(event: ChangeEvent<HTMLSelectElement>) {
    selectEnv(event.target.value as TopicEnv);
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
