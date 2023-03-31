import { RadioButton as BaseRadioButton } from "@aivenio/aquarium";
import { RadioButtonGroup } from "src/app/components/Form";

interface AclIpPrincipleTypeFieldProps {
  isAivenCluster?: boolean;
}

const AclIpPrincipleTypeField = ({
  isAivenCluster,
}: AclIpPrincipleTypeFieldProps) => {
  const principalLabelText = isAivenCluster ? "Service account" : "Principal";

  return (
    <RadioButtonGroup
      name="aclIpPrincipleType"
      labelText={`IP or ${principalLabelText} based`}
      // If isAivenCluster is undefined, we have not yet fetched it (no environment selected)
      // So all the options are disabled
      disabled={isAivenCluster || isAivenCluster === undefined}
    >
      <BaseRadioButton value="IP_ADDRESS" disabled={isAivenCluster}>
        IP
      </BaseRadioButton>
      <BaseRadioButton value="PRINCIPAL">{principalLabelText}</BaseRadioButton>
    </RadioButtonGroup>
  );
};

export default AclIpPrincipleTypeField;
