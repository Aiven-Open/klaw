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
  const labelText = readOnly ? "Topic name (read-only)" : "Topic name";
  return (
    <NativeSelect
      name="topicname"
      labelText={labelText}
      placeholder={"-- Select Topic --"}
      readOnly={readOnly}
      required={!readOnly}
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
