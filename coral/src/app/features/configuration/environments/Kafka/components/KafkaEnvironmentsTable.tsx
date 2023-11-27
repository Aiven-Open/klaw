import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  StatusChip,
} from "@aivenio/aquarium";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
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
  replicationFactor: { default: string; max: string };
  partition: { default: string; max: string };
  status: Environment["envStatus"];
  envStatusTimeString: Environment["envStatusTimeString"];
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
          <Box.Flex flexWrap={"wrap"} gap={"2"} component={"ul"}>
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
          <Box.Flex flexWrap={"wrap"} gap={"2"} component={"ul"}>
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
      width: 450,
      UNSAFE_render: ({
        status,
        id,
        envStatusTimeString,
      }: KafkaEnvironmentsTableRow) => {
        return (
          <EnvironmentStatus
            envId={id}
            initialEnvStatus={status}
            initialUpdateTime={envStatusTimeString}
          />
        );
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
        default: env.params?.defaultRepFactor || "0",
        max: env.params?.maxRepFactor || "0",
      },
      partition: {
        default: env.params?.defaultPartitions || "0",
        max: env.params?.maxPartitions || "0",
      },
      status: env.envStatus,
      envStatusTimeString: env.envStatusTimeString,
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
