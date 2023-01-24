import { MultiInput } from "src/app/components/Form";
import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
} from "src/domain/acl";
import { ClusterInfo } from "src/domain/environment";

interface IpOrPrincipalFieldProps {
  aclIpPrincipleType?:
    | CreateAclRequestTopicTypeProducer["aclIpPrincipleType"]
    | CreateAclRequestTopicTypeConsumer["aclIpPrincipleType"];
  clusterInfo: ClusterInfo;
}

const IpOrPrincipalField = ({
  aclIpPrincipleType,
  clusterInfo,
}: IpOrPrincipalFieldProps) => {
  const isAivenCluster = clusterInfo.aivenCluster === "true";
  const sslLabelText = isAivenCluster
    ? "Service Accounts"
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
