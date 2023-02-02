import { MultiInput } from "src/app/components/Form";
import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
} from "src/domain/acl";

interface IpOrPrincipalFieldProps {
  aclIpPrincipleType?:
    | CreateAclRequestTopicTypeProducer["aclIpPrincipleType"]
    | CreateAclRequestTopicTypeConsumer["aclIpPrincipleType"];
  isAivenCluster: boolean;
}

const IpOrPrincipalField = ({
  aclIpPrincipleType,
  isAivenCluster,
}: IpOrPrincipalFieldProps) => {
  const sslLabelText = isAivenCluster
    ? "Service accounts"
    : "SSL DN strings / Usernames";
  const sslPlaceholder = isAivenCluster ? "Alice" : "CN=myhost, Alice";

  if (aclIpPrincipleType === "IP_ADDRESS") {
    return (
      <MultiInput
        key="acl_ip"
        name="acl_ip"
        labelText="IP addresses"
        placeholder="192.168.1.1, 2606:4700:4700::1111"
        required
      />
    );
  }

  return (
    <MultiInput
      key="acl_ssl"
      name="acl_ssl"
      labelText={sslLabelText}
      placeholder={sslPlaceholder}
      required
    />
  );
};

export default IpOrPrincipalField;
