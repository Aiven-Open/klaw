import {
  Box,
  DataTable,
  DataTableColumn,
  EmptyState,
  StatusChip,
} from "@aivenio/aquarium";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
import { Environment } from "src/domain/environment";
import Highlighter from "react-highlight-words";

type KafkaEnvironmentsTableProps = {
  environments: Environment[];
  ariaLabel: string;
  search: string;
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
  const { environments, ariaLabel, search } = props;

  const columns: Array<DataTableColumn<KafkaEnvironmentsTableRow>> = [
    {
      type: "custom",
      headerName: "Environment",
      UNSAFE_render: ({ environmentName }: KafkaEnvironmentsTableRow) => (
        <Highlighter
          highlightClassName="YourHighlightClass"
          searchWords={[search]}
          autoEscape={true}
          textToHighlight={environmentName}
        />
      ),
    },
    {
      type: "custom",
      headerName: "Cluster",
      UNSAFE_render: ({ clusterName }: KafkaEnvironmentsTableRow) => (
        <Highlighter
          highlightClassName="YourHighlightClass"
          searchWords={[search]}
          autoEscape={true}
          textToHighlight={clusterName}
        />
      ),
    },
    {
      type: "custom",
      headerName: "Tenant",
      UNSAFE_render: ({ tenantName }: KafkaEnvironmentsTableRow) => (
        <Highlighter
          highlightClassName="YourHighlightClass"
          searchWords={[search]}
          autoEscape={true}
          textToHighlight={tenantName}
        />
      ),
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
      width: 150,
      UNSAFE_render: ({ status, id }: KafkaEnvironmentsTableRow) => {
        return <EnvironmentStatus envId={id} initialEnvStatus={status} />;
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
