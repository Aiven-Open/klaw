import { Box, Grid, StatusChip, Typography } from "@aivenio/aquarium";
import React from "react";
import { ClusterDetails as ClusterDetailsType } from "src/domain/cluster";

function DefinitionBlock({
  term,
  definition,
}: {
  term: string;
  definition: React.ReactNode | string;
}) {
  return (
    <Box.Flex flexDirection={"column"}>
      <dt className="inline-block mb-2 typography-small-strong text-grey-60">
        {term}
      </dt>

      <Typography.Small htmlTag={"dd"}>{definition}</Typography.Small>
    </Box.Flex>
  );
}

type ClusterDetailsProps = {
  clusterDetails: ClusterDetailsType;
};

function ClusterDetails({ clusterDetails }: ClusterDetailsProps) {
  return (
    <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
      <DefinitionBlock
        term={"Cluster name"}
        definition={clusterDetails.clusterName}
      />
      <DefinitionBlock
        term={"Cluster id"}
        definition={clusterDetails.clusterId}
      />

      <DefinitionBlock
        term={"Bootstrap server"}
        definition={clusterDetails.bootstrapServers}
      />
      <DefinitionBlock term={"Protocol"} definition={clusterDetails.protocol} />

      <DefinitionBlock
        term={"Type"}
        definition={
          <StatusChip status={"info"} text={clusterDetails.clusterType} />
        }
      />
      <DefinitionBlock
        term={"Kafka flavor"}
        definition={clusterDetails.kafkaFlavor}
      />

      {clusterDetails.associatedServers !== undefined && (
        <DefinitionBlock
          term={"Rest API"}
          definition={clusterDetails.associatedServers}
        />
      )}
    </Grid>
  );
}

export { ClusterDetails };
