import { Alert, Box, Input } from "@aivenio/aquarium";
import { useQuery } from "@tanstack/react-query";
import { TenantInformationData } from "src/app/features/user-information/tenant-information/TenantInformationData";
import { TenantInformationForm } from "src/app/features/user-information/tenant-information/TenantInformationForm";
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
      {data.authorizedToDelete ? (
        <TenantInformationForm />
      ) : (
        <TenantInformationData
          tenantName={data.tenantName}
          orgName={data.orgName}
          contactPerson={data.contactPerson}
        />
      )}
    </>
  );
};

export { TenantInformation };
