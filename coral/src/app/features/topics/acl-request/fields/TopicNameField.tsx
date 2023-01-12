import { Option } from "@aivenio/aquarium";
import { NativeSelect } from "src/app/components/Form";

interface TopicNameFieldProps {
  topicNames: string[];
}

const TopicNameField = ({ topicNames }: TopicNameFieldProps) => {
  return (
    <NativeSelect name="topicname" labelText="Topic name" required>
      <Option key={"Placeholder"} disabled>
        -- Select Topic --
      </Option>
      {topicNames.map((name) => (
        <Option key={name} value={name}>
          {name}
        </Option>
      ))}
    </NativeSelect>
  );
};

export default TopicNameField;
