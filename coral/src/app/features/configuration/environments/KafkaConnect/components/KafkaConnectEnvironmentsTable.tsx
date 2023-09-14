import { DataTable, DataTableColumn, EmptyState } from "@aivenio/aquarium";
import EnvironmentStatus from "src/app/features/configuration/environments/components/EnvironmentStatus";
import { Environment } from "src/domain/environment";

type KafkaConnectEnvironmentsTableProps = {
  environments: Environment[];
  ariaLabel: string;
};

interface KafkaConnectEnvironmentsTableRow {
  id: Environment["id"];
  environmentName: Environment["name"];
  clusterName: Environment["clusterName"];
  tenantName: Environment["tenantName"];
  status: Environment["envStatus"];
}

const KafkaConnectEnvironmentsTable = (
  props: KafkaConnectEnvironmentsTableProps
) => {
  const { environments, ariaLabel } = props;

  const columns: Array<DataTableColumn<KafkaConnectEnvironmentsTableRow>> = [
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
      headerName: "Status",
      width: 150,
      UNSAFE_render: ({ status, id }: KafkaConnectEnvironmentsTableRow) => {
        return <EnvironmentStatus envId={id} initialEnvStatus={status} />;
      },
    },
  ];

  const rows: KafkaConnectEnvironmentsTableRow[] = environments.map((env) => {
    return {
      id: env.id,
      environmentName: env.name,
      clusterName: env.clusterName,
      tenantName: env.tenantName,
      status: env.envStatus,
    };
  });

  if (rows.length === 0) {
    return (
      <EmptyState title="No Kafka Connect Environments">
        No Kafka Connect Environment matched your criteria.
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

export default KafkaConnectEnvironmentsTable;
