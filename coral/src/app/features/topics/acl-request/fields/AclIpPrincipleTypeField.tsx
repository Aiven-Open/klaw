import { RadioButton as BaseRadioButton } from "@aivenio/aquarium";
import { RadioButtonGroup } from "src/app/components/Form";

interface AclIpPrincipleTypeFieldProps {
  isAivenCluster: boolean;
}

const AclIpPrincipleTypeField = ({
  isAivenCluster,
}: AclIpPrincipleTypeFieldProps) => {
  return (
    <RadioButtonGroup
      name="aclIpPrincipleType"
      labelText="IP or Principal based"
      required
    >
      <BaseRadioButton value="IP_ADDRESS" disabled={isAivenCluster}>
        IP
      </BaseRadioButton>
      <BaseRadioButton value="PRINCIPAL">Principal</BaseRadioButton>
    </RadioButtonGroup>
  );
};

export default AclIpPrincipleTypeField;
