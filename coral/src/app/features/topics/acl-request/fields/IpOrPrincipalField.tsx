import { Box } from "@aivenio/aquarium";
import { MultiInput } from "src/app/components/Form";
import {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
} from "src/domain/acl";

interface IpOrPrincipalFieldProps {
  aclIpPrincipleType?:
    | CreateAclRequestTopicTypeProducer["aclIpPrincipleType"]
    | CreateAclRequestTopicTypeConsumer["aclIpPrincipleType"];
}

const IpOrPrincipalField = ({
  aclIpPrincipleType,
}: IpOrPrincipalFieldProps) => {
  if (aclIpPrincipleType === "IP_ADDRESS") {
    return (
      <MultiInput
        name="acl_ip"
        labelText="IP addresses"
        placeholder="192.168.1.1, 2606:4700:4700::1111"
        required
      />
    );
  }

  if (aclIpPrincipleType === "PRINCIPAL") {
    return (
      <MultiInput
        name="acl_ssl"
        labelText="SSL DN strings / Usernames"
        placeholder="CN=myhost, Alice"
        required
      />
    );
  }

  // Return empty element matching the height of other inputs to prevent layout shift
  return <Box data-testid={"empty"} style={{ height: "87px" }} />;
};

export default IpOrPrincipalField;
