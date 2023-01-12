import { Box } from "@aivenio/aquarium";
import { MultiInput } from "src/app/components/Form";
import {
  CreateAclRequestTopicTypeProducer,
  CreateAclRequestTopicTypeConsumer,
} from "src/domain/acl";

interface IpOrPrincipalFieldProps {
  aclIpPrincipleType:
    | CreateAclRequestTopicTypeProducer["aclIpPrincipleType"]
    | CreateAclRequestTopicTypeConsumer["aclIpPrincipleType"];
}

const IpOrPrincipalField = ({
  aclIpPrincipleType,
}: IpOrPrincipalFieldProps) => {
  if (aclIpPrincipleType === undefined) {
    return <Box style={{ height: "87px" }} />;
  }

  return aclIpPrincipleType === "IP_ADDRESS" ? (
    <MultiInput
      name="acl_ip"
      labelText="IP addresses"
      placeholder="192.168.1.1, 2606:4700:4700::1111"
      required
    />
  ) : (
    <MultiInput
      name="acl_ssl"
      labelText="SSL DN strings / Usernames"
      placeholder="CN=myhost, Alice"
      required
    />
  );
};

export default IpOrPrincipalField;
