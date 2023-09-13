import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  StatusChip,
} from "@aivenio/aquarium";
import { Environment } from "src/domain/environment";

type KafkaEnvironmentsTableProps = {
  environments: Environment[];
  ariaLabel: string;
};

interface KafkaEnvironmentsTableRow {
  id: Environment["id"];
  environmentName: Environment["name"];
  clusterName: Environment["clusterName"];
  tenantName: Environment["tenantName"];
  replicationFactor: { default: number; max: number };
  partition: { default: number; max: number };
  status: Environment["envStatus"];
}

const KafkaEnvironmentsTable = (props: KafkaEnvironmentsTableProps) => {
  const { environments, ariaLabel } = props;

  const columns: Array<DataTableColumn<KafkaEnvironmentsTableRow>> = [
    {
      type: "text",
      field: "environmentName",
      headerName: "Environment",
    },
    {
      type: "text",
      field: "clusterName",
      headerName: "Cluster",
    },
    {
      type: "text",
      field: "tenantName",
      headerName: "Tenant",
    },
    {
      type: "custom",
      headerName: "Replication factor",
      UNSAFE_render: ({ replicationFactor }: KafkaEnvironmentsTableRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            <li>
              <StatusChip
                dense
                status="neutral"
                text={`Default: ${replicationFactor.default}`}
              />
            </li>
            <li>
              <StatusChip
                dense
                status="neutral"
                text={`Max: ${replicationFactor.max}`}
              />
            </li>
          </Box.Flex>
        );
      },
    },
    {
      type: "custom",
      headerName: "Partition",
      UNSAFE_render: ({ partition }: KafkaEnvironmentsTableRow) => {
        return (
          <Box.Flex wrap={"wrap"} gap={"2"} component={"ul"}>
            <li>
              <StatusChip
                dense
                status="neutral"
                text={`Default: ${partition.default}`}
              />
            </li>
            <li>
              <StatusChip
                dense
                status="neutral"
                text={`Max: ${partition.max}`}
              />
            </li>
          </Box.Flex>
        );
      },
    },
    {
      type: "custom",
      headerName: "Status",
      UNSAFE_render: ({ status }: KafkaEnvironmentsTableRow) => {
        if (status === "OFFLINE") {
          return <StatusChip dense status="danger" text="Not working" />;
        }
        if (status === "ONLINE") {
          return <StatusChip dense status="success" text="Working" />;
        }
        if (status === "NOT_KNOWN") {
          return <StatusChip dense status="neutral" text="Unknown" />;
        }
      },
    },
  ];

  const rows: KafkaEnvironmentsTableRow[] = environments.map((env) => {
    return {
      id: env.id,
      environmentName: env.name,
      clusterName: env.clusterName,
      tenantName: env.tenantName,
      replicationFactor: {
        default: env.params?.defaultRepFactor || 0,
        max: env.params?.maxRepFactor || 0,
      },
      partition: {
        default: env.params?.defaultPartitions || 0,
        max: env.params?.maxPartitions || 0,
      },
      status: env.envStatus,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No Kafka Environments">
        No Kafka Environment matched your criteria.
      </EmptyState>
    );
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

export default KafkaEnvironmentsTable;
