import {
  Button,
  Grid,
  Skeleton,
  StatusChip,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import refresh from "@aivenio/aquarium/icons/refresh";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Environment, getUpdateEnvStatus } from "src/domain/environment";
import { parseErrorMsg } from "src/services/mutation-utils";

interface EnvironmentStatusProps {
  envId: Environment["id"];
  initialEnvStatus: Environment["envStatus"];
  initialUpdateTime: Environment["envStatusTimeString"];
}

const EnvironmentStatus = ({
  envId,
  initialEnvStatus,
  initialUpdateTime,
}: EnvironmentStatusProps) => {
  const toast = useToast();
  const [shoudlUpdateStatus, setShouldUpdateStatus] = useState(false);
  const [envStatus, setEnvStatus] =
    useState<Environment["envStatus"]>(initialEnvStatus);
  const [updateTime, setUpdateTime] = useState(initialUpdateTime || "Unknown");

  const {
    data: updatedEnvStatus,
    isFetching,
    isSuccess,
    isError,
    error,
  } = useQuery(["getUpdateEnvStatus", envId], {
    queryFn: () => getUpdateEnvStatus({ envId }),
    enabled: shoudlUpdateStatus,
  });

  useEffect(() => {
    if (!isFetching && isSuccess) {
      setShouldUpdateStatus(false);
      setEnvStatus(updatedEnvStatus.envStatus);
      setUpdateTime(updatedEnvStatus.envStatusTimeString);
    }
    if (!isFetching && isError) {
      setShouldUpdateStatus(false);
      toast({
        message: `Could not refresh Environment status: ${parseErrorMsg(
          error
        )}`,
        position: "bottom-left",
        variant: "danger",
      });
    }
  }, [isSuccess, isError, isFetching]);

  if (isFetching) {
    return (
      <Grid
        justifyContent={"stretch"}
        alignItems="center"
        colGap="3"
        style={{ gridTemplateColumns: "100px 280px 20px" }}
      >
        <Grid.Item>
          <StatusChip dense status="neutral" text="Refreshing..." />
        </Grid.Item>
        <Grid.Item>
          <Skeleton width={275} height={20} />
        </Grid.Item>
        <Button.Icon
          icon={loading}
          onClick={() => setShouldUpdateStatus(true)}
          aria-label={"Refreshing Environment status"}
          tooltip={"Refreshing Environment status"}
          disabled
        />
      </Grid>
    );
  }

  if (envStatus === "OFFLINE") {
    return (
      <Grid
        justifyContent={"stretch"}
        alignItems="center"
        colGap="3"
        style={{ gridTemplateColumns: "100px 280px 20px" }}
      >
        <Grid.Item>
          <StatusChip dense status="danger" text="Offline" />
        </Grid.Item>
        <Grid.Item>
          <Typography.Small>
            Last update:{" "}
            {updateTime === "Unknown" ? updateTime : `${updateTime} UTC`}
          </Typography.Small>
        </Grid.Item>
        <Button.Icon
          icon={refresh}
          onClick={() => setShouldUpdateStatus(true)}
          aria-label={"Refresh Environment status"}
          tooltip={"Refresh Environment status"}
        />
      </Grid>
    );
  }
  if (envStatus === "ONLINE") {
    return (
      <Grid
        justifyContent={"stretch"}
        alignItems="center"
        colGap="3"
        style={{ gridTemplateColumns: "100px 280px 20px" }}
      >
        <Grid.Item>
          <StatusChip dense status="success" text="Online" />
        </Grid.Item>
        <Grid.Item>
          <Typography.Small>
            Last update:{" "}
            {updateTime === "Unknown" ? updateTime : `${updateTime} UTC`}
          </Typography.Small>
        </Grid.Item>
        <Button.Icon
          icon={refresh}
          onClick={() => setShouldUpdateStatus(true)}
          aria-label={"Refresh Environment status"}
          tooltip={"Refresh Environment status"}
        />
      </Grid>
    );
  }

  return (
    <Grid
      justifyContent={"stretch"}
      alignItems="center"
      colGap="3"
      style={{ gridTemplateColumns: "100px 280px 20px" }}
    >
      <Grid.Item>
        <StatusChip dense status="neutral" text="Unknown" />
      </Grid.Item>
      <Grid.Item>
        <Typography.Small>
          Last update:{" "}
          {updateTime === "Unknown" ? updateTime : `${updateTime} UTC`}
        </Typography.Small>
      </Grid.Item>
      <Button.Icon
        icon={refresh}
        onClick={() => setShouldUpdateStatus(true)}
        aria-label={"Refresh Environment status"}
        tooltip={"Refresh Environment status"}
      />
    </Grid>
  );
};

export default EnvironmentStatus;
