import { Box, Typography } from "@aivenio/aquarium";

interface StatsDisplayProps {
  amount: number;
  entity: string;
}

const StatsDisplay = ({ amount, entity }: StatsDisplayProps) => {
  return (
    <Box.Flex flexDirection="column">
      <Typography.Heading>{amount}</Typography.Heading>
      <Typography.Small color={"grey-50"}>{entity}</Typography.Small>
    </Box.Flex>
  );
};

export default StatsDisplay;
