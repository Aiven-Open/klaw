import {
  Box,
  ChipStatus,
  Skeleton,
  StatusChip,
  Typography,
} from "@aivenio/aquarium";

interface StatsDisplayProps {
  isLoading: boolean;
  amount?: number;
  entity: string;
  chip?: {
    status: ChipStatus;
    text: string;
  };
}

const StatsDisplay = ({
  isLoading,
  amount,
  chip,
  entity,
}: StatsDisplayProps) => {
  if (!isLoading && chip === undefined && amount === undefined) {
    console.error("Define a chip or amount.");
  }

  if (!isLoading && chip !== undefined && amount !== undefined) {
    console.error("Define only either a chip or an amount.");
  }

  return (
    <Box flexDirection="column" justifyContent={"space-around"}>
      {isLoading && (
        <Typography.Heading htmlTag={"div"}>
          <span className={"visually-hidden"}>Loading information</span>
          <Skeleton />
        </Typography.Heading>
      )}
      {!isLoading && amount !== undefined && (
        <Typography.Heading htmlTag={"div"}>{amount}</Typography.Heading>
      )}
      {!isLoading && chip !== undefined && (
        <div>
          <StatusChip text={chip.text} status={chip.status} />
        </div>
      )}

      <Typography.Small color={"grey-50"}>{entity}</Typography.Small>
    </Box>
  );
};

export default StatsDisplay;
