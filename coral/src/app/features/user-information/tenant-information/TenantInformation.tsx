import { Alert, Box, Input } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { TenantInformationData } from "src/app/features/user-information/tenant-information/TenantInformationData";
import { getMyTenantInfo } from "src/domain/user/user-api";
import { parseErrorMsg } from "src/services/mutation-utils";

const TenantInformation = () => {
  const { data, isLoading, isError, error } = useQuery(
    ["getMyTenantInfo"],
    getMyTenantInfo
  );

  if (isLoading) {
    return (
      <Box.Flex flexDirection="column" maxWidth={"lg"}>
        <Input.Skeleton />
        <Input.Skeleton />
        <Input.Skeleton />
      </Box.Flex>
    );
  }

  if (isError) {
    return <Alert type="error">{parseErrorMsg(error)}</Alert>;
  }

  return (
    <TenantInformationData
      tenantName={data.tenantName}
      orgName={data.orgName}
      contactPerson={data.contactPerson}
      description={data.tenantDesc}
    />
  );
};

export { TenantInformation };
