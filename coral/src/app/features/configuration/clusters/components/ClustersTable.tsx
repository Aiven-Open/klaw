import { Box, DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import { ClusterDetails } from "src/domain/cluster";

type ClustersTableProps = {
  clusters: ClusterDetails[];
  ariaLabel: string;
};

interface ClustersTableRow {
  id: ClusterDetails["clusterId"];
  clusterName: ClusterDetails["clusterName"];
  bootstrapServers: ClusterDetails["bootstrapServers"];
  protocol: ClusterDetails["protocol"];
  clusterType: ClusterDetails["clusterType"];
  kafkaFlavor: ClusterDetails["kafkaFlavor"];
  restApiServer: ClusterDetails["associatedServers"];
  otherParams: {
    serviceName: ClusterDetails["serviceName"];
    projectName: ClusterDetails["projectName"];
  };
}

const ClustersTable = (props: ClustersTableProps) => {
  const { clusters, ariaLabel } = props;

  const columns: Array<DataTableColumn<ClustersTableRow>> = [
    {
      type: "status",
      headerName: "Cluster",
      status: ({ clusterName }) => ({
        text: clusterName,
        status: "neutral",
      }),
    },
    {
      type: "text",
      field: "bootstrapServers",
      headerName: "Bootstrap servers",
    },
    {
      type: "text",
      field: "protocol",
      headerName: "Protocol",
    },
    {
      type: "status",
      headerName: "Type",
      status: ({ clusterType }) => ({
        text: clusterType,
        status: "neutral",
      }),
    },
    {
      type: "text",
      field: "kafkaFlavor",
      headerName: "Kafka flavor",
      width: 180,
    },
    {
      type: "text",
      field: "restApiServer",
      headerName: "RestApi server",
    },
    {
      type: "custom",
      headerName: "Other params",
      UNSAFE_render: ({ otherParams }: ClustersTableRow) => {
        if (!otherParams.projectName && !otherParams.serviceName) {
          return <div>-NA-</div>;
        }
        return (
          <Box.Flex flexWrap={"wrap"} gap={"2"} component={"ul"}>
            {otherParams.projectName && (
              <span>projectName={otherParams.projectName}</span>
            )}
            {otherParams.serviceName && (
              <span>serviceName={otherParams.serviceName}</span>
            )}
          </Box.Flex>
        );
      },
    },
  ];

  const rows: ClustersTableRow[] = clusters.map((cluster) => {
    return {
      id: cluster.clusterId,
      clusterName: cluster.clusterName,
      bootstrapServers: cluster.bootstrapServers,
      protocol: cluster.protocol,
      clusterType: cluster.clusterType,
      kafkaFlavor: cluster.kafkaFlavor,
      restApiServer: cluster.associatedServers,
      otherParams: {
        serviceName: cluster.serviceName,
        projectName: cluster.projectName,
      },
    };
  });

  if (rows.length === 0) {
    return <EmptyState title="No Clusters">No clusters found.</EmptyState>;
  }

  return (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};

export { ClustersTable };
