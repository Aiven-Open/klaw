import { Alert, Box, Input } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { TenantInformationData } from "src/app/features/user-information/tenant-information/TenantInformationData";
import { getMyTenantInfo } from "src/domain/user/user-api";
import { parseErrorMsg } from "src/services/mutation-utils";

const TenantInformation = () => {
  const { data, isError, error } = useQuery(
    ["getMyTenantInfo"],
    getMyTenantInfo
  );

  if (data === undefined) {
    return (
      <>
        {isError && <Alert type="error">{parseErrorMsg(error)}</Alert>}
        {!isError && (
          <Box.Flex flexDirection="column" maxWidth={"lg"}>
            <Box.Flex flexDirection="column">
              <Input.Skeleton />
            </Box.Flex>
            <Box.Flex flexDirection="column">
              <Input.Skeleton />
            </Box.Flex>
            <Box.Flex flexDirection="column">
              <Input.Skeleton />
            </Box.Flex>
          </Box.Flex>
        )}
      </>
    );
  }

  return (
    <>
      {isError && (
        <Box marginBottom={"l1"}>
          <Alert type="error">{parseErrorMsg(error)}</Alert>
        </Box>
      )}

      <TenantInformationData
        tenantName={data.tenantName}
        orgName={data.orgName}
        contactPerson={data.contactPerson}
        description={data.tenantDesc}
      />
    </>
  );
};

export { TenantInformation };
