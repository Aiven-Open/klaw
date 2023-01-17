import { RadioButton as BaseRadioButton } from "@aivenio/aquarium";
import { RadioButtonGroup } from "src/app/components/Form";
import { ClusterInfo } from "src/domain/environment";

interface AclIpPrincipleTypeFieldProps {
  clusterInfo?: ClusterInfo;
}

const AclIpPrincipleTypeField = ({
  clusterInfo,
}: AclIpPrincipleTypeFieldProps) => {
  const isAivenCluster = clusterInfo?.aivenCluster === "true";
  return (
    <RadioButtonGroup
      name="aclIpPrincipleType"
      labelText="IP or Principal based"
      required
      // If clusterInfo is undefined, we have not yet fetched it (no environment selected)
      // So all the options are disabled
      disabled={clusterInfo === undefined}
    >
      <BaseRadioButton value="IP_ADDRESS" disabled={isAivenCluster}>
        IP
      </BaseRadioButton>
      <BaseRadioButton value="PRINCIPAL">Principal</BaseRadioButton>
    </RadioButtonGroup>
  );
};

export default AclIpPrincipleTypeField;
