import { Box, Typography } from "@aivenio/aquarium";

interface TenantInformationDataProps {
  tenantName: string;
  orgName?: string;
  contactPerson?: string;
}

const TenantInformationData = ({
  tenantName,
  orgName,
  contactPerson,
}: TenantInformationDataProps) => {
  return (
    <Box.Flex flexDirection="column" gap={"l2"}>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">
          Tenant name
        </Typography.SmallStrong>
        <Typography.Default htmlTag="dd">{tenantName}</Typography.Default>
      </Box.Flex>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">
          Organization
        </Typography.SmallStrong>
        <Typography.Default htmlTag="dd">
          {orgName === undefined ? <i>Not available</i> : orgName}
        </Typography.Default>
      </Box.Flex>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">
          Contact person
        </Typography.SmallStrong>
        <Typography.Default htmlTag="dd">
          {contactPerson === undefined ? <i>Not available</i> : contactPerson}
        </Typography.Default>
      </Box.Flex>
    </Box.Flex>
  );
};

export { TenantInformationData };
