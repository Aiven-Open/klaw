import {
  Box,
  DataTable,
  DataTableColumn,
  DropdownMenu,
  EmptyState,
} from "@aivenio/aquarium";
import type {
  ClusterConnectModalState,
  ClusterDeleteModalState,
} from "src/app/features/configuration/clusters/Clusters";
import { ClusterDetails } from "src/domain/cluster";
import { clusterTypeToString } from "src/services/formatter/cluster-type-formatter";
import { kafkaFlavorToString } from "src/services/formatter/kafka-flavor-formatter";
import deleteIcon from "@aivenio/aquarium/dist/src/icons/delete";

type ClustersTableProps = {
  clusters: ClusterDetails[];
  ariaLabel: string;
  handleShowConnectModal?: ({ show, data }: ClusterConnectModalState) => void;
  handleShowDeleteModal?: ({ show, data }: ClusterDeleteModalState) => void;
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
  canDeleteCluster: ClusterDetails["showDeleteCluster"];
}

const ClustersTable = (props: ClustersTableProps) => {
  const { clusters, ariaLabel, handleShowConnectModal, handleShowDeleteModal } =
    props;

  const isAdminUser =
    handleShowConnectModal !== undefined && handleShowDeleteModal !== undefined;

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
      headerName: "Cluster type",
      status: ({ clusterType }) => ({
        text: clusterTypeToString[clusterType],
        status: "neutral",
      }),
    },
    {
      type: "text",
      field: "kafkaFlavor",
      headerName: "Kafka flavor",
      width: 180,
      formatter: (value) => kafkaFlavorToString[value],
    },
    {
      type: "text",
      field: "restApiServer",
      headerName: "REST API servers",
    },
    {
      type: "custom",
      headerName: "Other parameters",
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

  if (isAdminUser) {
    columns.push({
      type: "action",
      headerName: "",
      headerInvisible: true,
      action: ({ kafkaFlavor, protocol, clusterType, clusterName, id }) => ({
        text: "Connect",
        onClick: () =>
          handleShowConnectModal({
            show: true,
            data: {
              kafkaFlavor,
              protocol,
              clusterType,
              clusterName,
              clusterId: id,
            },
          }),
        "aria-label": `Show help for connecting the cluster ${clusterName} to Klaw`,
      }),
    });
  }

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
      canDeleteCluster: cluster.showDeleteCluster,
    };
  });

  if (rows.length === 0) {
    return <EmptyState title="No Clusters">No clusters found.</EmptyState>;
  }

  return isAdminUser ? (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
      menu={
        <DropdownMenu.Items>
          <DropdownMenu.Item icon={deleteIcon} key="delete">
            Remove
          </DropdownMenu.Item>
        </DropdownMenu.Items>
      }
      onAction={(action, data): void => {
        if (action === "delete") {
          handleShowDeleteModal({
            show: true,
            data: {
              clusterId: data.id,
              clusterName: data.clusterName,
              canDeleteCluster: data.canDeleteCluster,
            },
          });
        }
      }}
    />
  ) : (
    <DataTable
      ariaLabel={ariaLabel}
      columns={columns}
      rows={rows}
      noWrap={false}
    />
  );
};

export { ClustersTable };
