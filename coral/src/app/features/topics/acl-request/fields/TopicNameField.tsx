import { Combobox } from "src/app/components/Form";

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
    <Combobox
      name="topicname"
      labelText={labelText}
      placeholder={"-- Select Topic --"}
      readOnly={readOnly}
      required={!readOnly}
      options={topicNames}
    />
  );
};

export default TopicNameField;
