import { NativeSelect, Option } from "@aivenio/design-system";
import { ChangeEvent } from "react";

type SelectTeamProps = {
  teamOptions: Array<{ label: string; value: string }>;
  activeOption: string;
  selectTeam: (value: string) => void;
};
function SelectTeam(props: SelectTeamProps) {
  const { teamOptions, activeOption, selectTeam } = props;

  function onChangeEnv(event: ChangeEvent<HTMLSelectElement>) {
    selectTeam(event.target.value);
  }

  return (
    <NativeSelect
      labelText="Team"
      value={activeOption}
      onChange={(event) => onChangeEnv(event)}
    >
      {teamOptions.map(({ label, value }) => (
        <Option key={value} value={value}>
          {label}
        </Option>
      ))}
    </NativeSelect>
  );
}

export default SelectTeam;
