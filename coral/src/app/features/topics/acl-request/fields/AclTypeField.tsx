import {
  RadioButton as BaseRadioButton,
  RadioButtonGroup as BaseRadioButtonGroup,
} from "@aivenio/aquarium";

interface AclTypeFieldProps {
  handleChange: (value: string) => void;
  topicType: string;
}

const AclTypeField = ({ handleChange, topicType }: AclTypeFieldProps) => (
  <BaseRadioButtonGroup
    labelText="ACL type"
    name="topicType"
    onChange={handleChange}
    required
  >
    <BaseRadioButton value="Producer" checked={topicType === "Producer"}>
      Producer
    </BaseRadioButton>
    <BaseRadioButton value="Consumer" checked={topicType === "Consumer"}>
      Consumer
    </BaseRadioButton>
  </BaseRadioButtonGroup>
);

export default AclTypeField;
