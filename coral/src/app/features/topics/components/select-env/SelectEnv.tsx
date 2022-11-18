import { NativeSelect, Option } from "@aivenio/design-system";
import { ChangeEvent } from "react";
import { TopicEnv } from "src/domain/topics";

type SelectEnvProps = {
  envOptions: Array<TopicEnv>;
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
      {envOptions.map((env) => {
        return (
          <Option key={env} value={env}>
            {env}
          </Option>
        );
      })}
    </NativeSelect>
  );
}

export default SelectEnv;
