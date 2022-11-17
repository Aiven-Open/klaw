import { Select } from "@aivenio/design-system";

type Env = "ALL" | "DEV" | "TST";
type SelectEnvProps = {
  envOptions: Array<Env>;
  activeOption: Env;
  selectEnv: (value: Env) => void;
};
function SelectEnv(props: SelectEnvProps) {
  const { envOptions, activeOption, selectEnv } = props;

  function onChangeEnv(value: Env | null | undefined) {
    if (value) {
      selectEnv(value);
    }
  }

  return (
    <Select
      labelText="Kafka Environment"
      value={activeOption}
      options={envOptions}
      onChange={onChangeEnv}
    />
  );
}

export default SelectEnv;
