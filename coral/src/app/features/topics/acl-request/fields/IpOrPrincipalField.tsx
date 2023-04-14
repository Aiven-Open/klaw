import { MultiSelect as BaseMultiSelect } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { MultiInput, MultiSelect } from "src/app/components/Form";
import {
  CreateAclRequestTopicTypeConsumer,
  CreateAclRequestTopicTypeProducer,
} from "src/domain/acl";
import { getAivenServiceAccounts } from "src/domain/acl/acl-api";

interface IpOrPrincipalFieldProps {
  aclIpPrincipleType?:
    | CreateAclRequestTopicTypeProducer["aclIpPrincipleType"]
    | CreateAclRequestTopicTypeConsumer["aclIpPrincipleType"];
  isAivenCluster: boolean;
  environment: string;
}

const IpOrPrincipalField = ({
  aclIpPrincipleType,
  isAivenCluster,
  environment,
}: IpOrPrincipalFieldProps) => {
  const {
    data = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["getAivenServiceAccounts", environment],
    queryFn: () =>
      getAivenServiceAccounts({
        env: environment,
      }),
    enabled: isAivenCluster,
  });

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

  if (isAivenCluster) {
    // Fall back to default field if error during fetching
    if (isError) {
      return (
        <MultiInput
          key="acl_ssl"
          name="acl_ssl"
          labelText={sslLabelText}
          placeholder={sslPlaceholder}
          required
        />
      );
    }

    if (isLoading) {
      return (
        <div data-testid={"acl_ssl-skeleton"}>
          <BaseMultiSelect.Skeleton />
        </div>
      );
    }

    return (
      <MultiSelect<{ acl_ssl: string[] }, string>
        key="acl_ssl"
        name="acl_ssl"
        labelText={sslLabelText}
        placeholder={
          "Select an existing account or enter a new one to create it"
        }
        options={data}
        // Allow adding new service accounts
        createOption={(newOption) => {
          if (newOption === undefined) {
            return;
          }
          return newOption;
        }}
        noResults={"No service account matches."}
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
