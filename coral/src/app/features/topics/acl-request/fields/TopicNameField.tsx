import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";

interface TopicNameFieldProps {
  topicNames: string[];
  readOnly?: boolean;
}

const TopicNameField = ({
  topicNames,
  readOnly = false,
}: TopicNameFieldProps) => {
  return (
    <NativeSelect
      name="topicname"
      labelText="Topic name"
      placeholder={"-- Select Topic --"}
      readOnly={readOnly}
      required
    >
      {topicNames.map((name) => (
        <Option key={name} value={name}>
          {name}
        </Option>
      ))}
    </NativeSelect>
  );
};

export default TopicNameField;
