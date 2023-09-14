import { useToast, Box, StatusChip, Button } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import refresh from "@aivenio/aquarium/icons/refresh";
import { useQuery } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { Environment, getUpdateEnvStatus } from "src/domain/environment";
import { parseErrorMsg } from "src/services/mutation-utils";

const EnvironmentStatus = ({
  envId,
  initialEnvStatus,
}: {
  envId: Environment["id"];
  initialEnvStatus: Environment["envStatus"];
}) => {
  const toast = useToast();
  const [shoudlUpdateStatus, setShouldUpdateStatus] = useState(false);
  const [envStatus, setEnvStatus] =
    useState<Environment["envStatus"]>(initialEnvStatus);

  const {
    data: updatedEnvStatus,
    isFetching,
    isRefetching,
    isSuccess,
    isError,
    error,
  } = useQuery(["getUpdateEnvStatus", envId], {
    queryFn: () => getUpdateEnvStatus({ envId }),
    enabled: shoudlUpdateStatus,
  });

  useEffect(() => {
    if (isSuccess) {
      setShouldUpdateStatus(false);
      setEnvStatus(updatedEnvStatus.envStatus);
    }
    if (isError) {
      setShouldUpdateStatus(false);
      toast({
        message: `Could not refresh Environment status: ${parseErrorMsg(
          error
        )}`,
        position: "bottom-left",
        variant: "danger",
      });
    }
  }, [isSuccess, isError]);

  if (isFetching || isRefetching) {
    return (
      <Box.Flex justifyContent="space-between">
        <StatusChip dense status="neutral" text="Refreshing..." />
        <Button.Icon
          icon={loading}
          onClick={() => setShouldUpdateStatus(true)}
          aria-label={"Refreshing Environment status"}
          tooltip={"Refreshing Environment status"}
          disabled
        />
      </Box.Flex>
    );
  }

  if (envStatus === "OFFLINE") {
    return (
      <Box.Flex justifyContent="space-between">
        <StatusChip dense status="danger" text="Not working" />
        <Button.Icon
          icon={refresh}
          onClick={() => setShouldUpdateStatus(true)}
          aria-label={"Refresh Environment status"}
          tooltip={"Refresh Environment status"}
        />
      </Box.Flex>
    );
  }
  if (envStatus === "ONLINE") {
    return (
      <Box.Flex justifyContent="space-between">
        <StatusChip dense status="success" text="Working" />
        <Button.Icon
          icon={refresh}
          onClick={() => setShouldUpdateStatus(true)}
          aria-label={"Refresh Environment status"}
          tooltip={"Refresh Environment status"}
        />
      </Box.Flex>
    );
  }

  return (
    <Box.Flex justifyContent="space-between">
      <StatusChip dense status="neutral" text="Unknown" />
      <Button.Icon
        icon={refresh}
        onClick={() => setShouldUpdateStatus(true)}
        aria-label={"Refresh Environment status"}
        tooltip={"Refresh Environment status"}
      />
    </Box.Flex>
  );
};

export default EnvironmentStatus;
