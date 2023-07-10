import { Box, Grid, Skeleton, StatusChip, Typography } from "@aivenio/aquarium";
import React from "react";
import { ClusterDetails as ClusterDetailsType } from "src/domain/cluster";

function DefinitionBlock({
  term,
  definition,
  isUpdating,
}: {
  term: string;
  definition: React.ReactNode | string;
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
  clusterDetails: ClusterDetailsType;
  isUpdating: boolean;
};

function ClusterDetails({ clusterDetails, isUpdating }: ClusterDetailsProps) {
  return (
    <>
      {isUpdating && (
        <div className={"visually-hidden"}>Cluster details are updating</div>
      )}
      <div aria-hidden={isUpdating}>
        <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
          <DefinitionBlock
            term={"Cluster name"}
            definition={clusterDetails.clusterName}
            isUpdating={isUpdating}
          />
          <DefinitionBlock
            term={"Cluster id"}
            definition={clusterDetails.clusterId}
            isUpdating={isUpdating}
          />

          <DefinitionBlock
            term={"Bootstrap server"}
            definition={clusterDetails.bootstrapServers}
            isUpdating={isUpdating}
          />
          <DefinitionBlock
            term={"Protocol"}
            definition={clusterDetails.protocol}
            isUpdating={isUpdating}
          />

          <DefinitionBlock
            term={"Type"}
            definition={
              <StatusChip status={"info"} text={clusterDetails.clusterType} />
            }
            isUpdating={isUpdating}
          />
          <DefinitionBlock
            term={"Kafka flavor"}
            definition={clusterDetails.kafkaFlavor}
            isUpdating={isUpdating}
          />

          {clusterDetails.associatedServers !== undefined && (
            <DefinitionBlock
              term={"Rest API"}
              definition={clusterDetails.associatedServers}
              isUpdating={isUpdating}
            />
          )}
        </Grid>
      </div>
    </>
  );
}

export { ClusterDetails };
