import { Box, Typography } from "@aivenio/aquarium";

interface TenantInformationDataProps {
  tenantName: string;
  orgName: string;
  contactPerson: string;
  description: string;
}

const TenantInformationData = ({
  tenantName,
  orgName,
  contactPerson,
  description,
}: TenantInformationDataProps) => {
  return (
    <Box.Flex flexDirection="column" gap={"l2"}>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">
          Description
        </Typography.SmallStrong>
        <Typography.Default htmlTag="dd">{description}</Typography.Default>
      </Box.Flex>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">
          Organization
        </Typography.SmallStrong>
        <Typography.Default htmlTag="dd">{orgName}</Typography.Default>
      </Box.Flex>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">Tenant</Typography.SmallStrong>
        <Typography.Default htmlTag="dd">{tenantName}</Typography.Default>
      </Box.Flex>
      <Box.Flex flexDirection="column">
        <Typography.SmallStrong htmlTag="dt">
          Contact person
        </Typography.SmallStrong>
        <Typography.Default htmlTag="dd">{contactPerson}</Typography.Default>
      </Box.Flex>
    </Box.Flex>
  );
};

export { TenantInformationData };
