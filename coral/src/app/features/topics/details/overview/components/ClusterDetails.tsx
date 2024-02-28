import {
  Box,
  ChipStatus,
  Grid,
  Skeleton,
  StatusChip,
  Typography,
} from "@aivenio/aquarium";
import React from "react";
import { ClusterDetails as ClusterDetailsType } from "src/domain/cluster";
import { kafkaFlavorToString } from "src/services/formatter/kafka-flavor-formatter";

function DefinitionBlock({
  term,
  definition,
  isUpdating,
  information,
}: {
  term: string;
  definition?: React.ReactNode | string;
  isUpdating: boolean;
  information?: string;
}) {
  return (
    <Box.Flex flexDirection={"column"}>
      <dt className="inline-block mb-2 typography-small-strong text-grey-60">
        {term}
      </dt>

      <Typography.Small htmlTag={"dd"}>
        {isUpdating ? <Skeleton /> : definition}
      </Typography.Small>
      {!isUpdating && information && (
        <Box marginTop={"2"} component={"dd"}>
          <Typography.Caption>{information}</Typography.Caption>
        </Box>
      )}
    </Box.Flex>
  );
}

function getChipStatus(
  status?: ClusterDetailsType["clusterStatus"]
): ChipStatus {
  if (status === "ONLINE") {
    return "success";
  }
  if (status === "OFFLINE") {
    return "danger";
  }
  return "neutral";
}

function getTextStatus(status?: ClusterDetailsType["clusterStatus"]): string {
  if (status === "ONLINE") {
    return "Online";
  }
  if (status === "OFFLINE") {
    return "Offline";
  }
  return "Not know";
}

type ClusterDetailsProps = {
  clusterDetails: ClusterDetailsType | undefined;
  isUpdating: boolean;
};

function ClusterDetails({ clusterDetails, isUpdating }: ClusterDetailsProps) {
  if (!isUpdating && clusterDetails === undefined) {
    console.error(
      "You must pass cluster details when the state of isUpdating is true"
    );
  }

  return (
    <>
      {isUpdating && (
        <div className={"visually-hidden"}>Cluster details are updating</div>
      )}
      <div aria-hidden={isUpdating}>
        <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
          <DefinitionBlock
            term={"Bootstrap server"}
            definition={clusterDetails?.bootstrapServers}
            isUpdating={isUpdating}
          />
          <DefinitionBlock
            term={"Protocol"}
            definition={clusterDetails?.protocol}
            isUpdating={isUpdating}
          />

          <DefinitionBlock
            term={"Kafka flavor"}
            definition={
              clusterDetails
                ? kafkaFlavorToString[clusterDetails.kafkaFlavor]
                : undefined
            }
            isUpdating={isUpdating}
          />
          <DefinitionBlock
            term={"Rest API"}
            definition={clusterDetails?.associatedServers || "Not applicable"}
            isUpdating={isUpdating}
          />

          <DefinitionBlock
            term={"Cluster name"}
            definition={clusterDetails?.clusterName}
            isUpdating={isUpdating}
          />

          <DefinitionBlock
            term={"Cluster status"}
            definition={
              <StatusChip
                text={getTextStatus(clusterDetails?.clusterStatus)}
                status={getChipStatus(
                  clusterDetails?.clusterStatus || "NOT_KNOWN"
                )}
              />
            }
            isUpdating={isUpdating}
            information={
              "Status is updated every hour. Contact your administrator to get a more current state."
            }
          />
        </Grid>
      </div>
    </>
  );
}

export { ClusterDetails };
