import {
  RadioButton as BaseRadioButton,
  RadioButtonGroup as BaseRadioButtonGroup,
} from "@aivenio/aquarium";
import { AclType } from "src/domain/acl";

interface AclTypeFieldProps {
  handleChange: (value: AclType) => void;
  aclType: AclType;
}

const AclTypeField = ({ handleChange, aclType }: AclTypeFieldProps) => (
  <BaseRadioButtonGroup
    labelText="ACL type"
    name="aclType"
    onChange={(value) => handleChange(value as AclType)}
    required
  >
    <BaseRadioButton value={"PRODUCER"} checked={aclType === "PRODUCER"}>
      Producer
    </BaseRadioButton>
    <BaseRadioButton value={"CONSUMER"} checked={aclType === "CONSUMER"}>
      Consumer
    </BaseRadioButton>
  </BaseRadioButtonGroup>
);

export default AclTypeField;
