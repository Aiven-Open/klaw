import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";

interface TopicNameFieldProps {
  topicNames: string[];
}

const TopicNameField = ({ topicNames }: TopicNameFieldProps) => {
  return (
    <NativeSelect
      name="topicname"
      labelText="Topic name"
      disabled={topicNames.length === 0}
      required
    >
      <Option disabled>-- Select Topic --</Option>
      {topicNames.map((name) => (
        <Option key={name} value={name}>
          {name}
        </Option>
      ))}
    </NativeSelect>
  );
};

export default TopicNameField;
