import { Box, Grid, Skeleton, Typography } from "@aivenio/aquarium";
import React from "react";
import { ClusterDetails as ClusterDetailsType } from "src/domain/cluster";

function DefinitionBlock({
  term,
  definition,
  isUpdating,
}: {
  term: string;
  definition?: React.ReactNode | string;
  isUpdating: boolean;
}) {
  return (
    <Box.Flex flexDirection={"column"}>
      <dt className="inline-block mb-2 typography-small-strong text-grey-60">
        {term}
      </dt>

      <Typography.Small htmlTag={"dd"}>
        {isUpdating ? <Skeleton /> : definition}
      </Typography.Small>
    </Box.Flex>
  );
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
            definition={clusterDetails?.kafkaFlavor}
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
        </Grid>
      </div>
    </>
  );
}

export { ClusterDetails };
